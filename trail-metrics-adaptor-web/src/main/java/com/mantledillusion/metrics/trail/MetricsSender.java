package com.mantledillusion.metrics.trail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.Collectors;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.web.*;

/**
 * {@link MetricsConsumer} implementation that is able to package and send
 * consumed {@link Metric}s via web service.
 */
public class MetricsSender implements MetricsConsumer {

	public static final int DEFAULT_MAX_RETRY_COUNT = 3;
	public static final int DEFAULT_MIN_ACCUMULATION_TIME = 500;
	public static final long[] SENDING_RETRY_INTERVALS = {
			// 5 Seconds
			5000,
			// 10 Seconds
			10000,
			// 15 Seconds
			15000 };

	/**
	 * Defines the modes the {@link MetricsSender} can operate on when deciding how
	 * to send {@link Metric}s.
	 */
	public enum SenderMode {

		/**
		 * Causes every incoming {@link Metric} to be wrapped into a
		 * {@link WebMetricRequest} on its own and given to the {@link MetricsWebFacade}
		 * directly.
		 */
		SYNCHRONOUS,

		/**
		 * Causes the first incoming {@link Metric} to trigger accumulating a variable
		 * amount of {@link Metric}s to wrap into a single {@link WebMetricRequest}.
		 * <p>
		 * Sending the request will be triggered automatically after an amount of time
		 * specifiable using {@link MetricsSender#setMinAccumulationTime(long)}.
		 * <p>
		 * Note that when sending a packaged request fails, sending is retried a
		 * specific amount of times specifiable using
		 * {@link MetricsSender#setMaxRetryCount(int)}. If that limit is exceeded, the
		 * sender will become locked, causing {@link MetricsSender#onLock()} to be
		 * called. The sender will refuse consuming any more elements until it is
		 * unlocked again using {@link MetricsSender#unlock()}.
		 */
		PACKAGED;
	}

	private class MetricsPackage {

		private final WebMetricRequest request = new WebMetricRequest();
		private final Map<String, WebMetricConsumer> consumerMapping = new HashMap<>();
		private final Map<String, Map<UUID, WebMetricTrail>> trailMapping = new HashMap<>();

		private void add(String consumerId, UUID trailId, WebMetric metric) {

			boolean beginTransfer = this.consumerMapping.isEmpty();

			WebMetricTrail trail = this.trailMapping.computeIfAbsent(consumerId, cid -> {
				WebMetricConsumer c = new WebMetricConsumer();
				c.setConsumerId(cid);
				this.request.getConsumers().add(c);
				this.consumerMapping.put(cid, c);
				return new HashMap<>();
			}).computeIfAbsent(trailId, sid -> {
				WebMetricTrail s = new WebMetricTrail();
				s.setTrailId(sid.toString());
				this.consumerMapping.get(consumerId).getTrails().add(s);
				return s;
			});

			trail.getMetrics().add(metric);

			if (beginTransfer) {
				transfer(MetricsSender.this.minAccumulationTime);
			}
		}

		void transfer(long sendDelay) {
			MetricsSender.this.delivererService.execute(() -> {
				MetricsSender.this.sendLock.lock();
				try {
					Thread.sleep(sendDelay);
					int tries = 0;

					while (true) {
						/*
						 * If we would try more often than allowed, we lock the sender and stop trying
						 * to send
						 */
						if (tries > MetricsSender.this.maxRetryCount) {
							MetricsSender.this.locked = true;
							break;
						}

						try {
							MetricsSender.this.facade.transfer(this.request);
							MetricsSender.this.pack = new MetricsPackage();
							break;
						} catch (Exception e) {
							/*
							 * If a request could not be delivered, we wait for the next time to try it.
							 */
							long retryIntervalMs = MetricsSender.this.sendingRetryIntervals[Math.min(tries + 1,
									MetricsSender.this.sendingRetryIntervals.length - 1)];

							Thread.sleep(retryIntervalMs);
							tries++;
						}
					}
				} catch (InterruptedException e) {
					MetricsSender.this.locked = true;
					throw new RuntimeException(
							"Unable to wait the desireable amount of time for accumulation/retrying to send.", e);
				} finally {
					MetricsSender.this.sendLock.unlock();

					if (MetricsSender.this.locked) {
						MetricsSender.this.onLock();
					}
				}
			});
		}
	}

	private final MetricsWebFacade facade;
	private final ThreadPoolExecutor delivererService = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	private final ReadLock packLock = this.lock.readLock();
	private final WriteLock sendLock = this.lock.writeLock();

	private MetricsPackage pack = new MetricsPackage();
	private boolean locked = false;

	private SenderMode mode = SenderMode.PACKAGED;
	private long minAccumulationTime = DEFAULT_MIN_ACCUMULATION_TIME;
	private long[] sendingRetryIntervals = SENDING_RETRY_INTERVALS;
	private int maxRetryCount = DEFAULT_MAX_RETRY_COUNT;

	/**
	 * Constructor.
	 * <p>
	 * Will use the given {@link MetricsWebFacade} to transfer packaged
	 * {@link Metric}s.
	 * 
	 * @param facade The facade that is able to transfer packaged {@link Metric}s
	 *               via web service when
	 *               {@link MetricsWebFacade#transfer(WebMetricRequest)}
	 *               is called; might <b>not</b> be null.
	 */
	protected MetricsSender(MetricsWebFacade facade) {
		if (facade == null) {
			throw new IllegalArgumentException("Cannot wrap a null facade");
		}
		this.facade = facade;
	}

	/**
	 * Is called when the {@link MetricsSender} becomes locked, which means that a
	 * readily accumulated {@link WebMetricRequest} could not be dispatched, even
	 * with the amount of retries set to {@link #setMaxRetryCount(int)}.
	 * <p>
	 * When this method is called, {@link #isLocked()} returns true.
	 * <p>
	 * A locked sender can only occur if the sender is operating on
	 * {@link SenderMode#PACKAGED}.
	 * <p>
	 * The default implementation does nothing.
	 */
	protected void onLock() {

	}

	/**
	 * Returns the number of {@link Metric}s currently being either packaged or
	 * synchronously send.
	 * 
	 * @return The count of {@link Metric}s awaiting, never &lt;0
	 */
	public int getAwaitingCount() {
		return this.lock.getReadHoldCount();
	}

	/**
	 * Returns whether a {@link SenderMode#PACKAGED} set of {@link Metric}s is
	 * currently being send.
	 * 
	 * @return True if an accumulated set of {@link Metric}s is currently being
	 *         send, false otherwise
	 */
	public boolean isSendingPackage() {
		return this.delivererService.getQueue().size() > 0 || this.delivererService.getActiveCount() > 0;
	}

	/**
	 * Sets the {@link SenderMode}.
	 * <p>
	 * The default mode is {@link SenderMode#PACKAGED}
	 * 
	 * @param mode The {@link SenderMode}; might <b>not</b> be null.
	 */
	public void setMode(SenderMode mode) {
		if (mode == null) {
			throw new IllegalArgumentException("Cannot set the sender mode to null");
		}
		this.mode = mode;
	}

	/**
	 * Sets the amount of time in milliseconds to minimally wait for more
	 * {@link Metric}s to arrive for consuming until a {@link WebMetricRequest} of
	 * all accumulated {@link Metric}s is send.
	 * <p>
	 * The default accumulation time is {@link #DEFAULT_MIN_ACCUMULATION_TIME}
	 * <p>
	 * Only has effect if the sender is operating on {@link SenderMode#PACKAGED}.
	 * 
	 * @param minAccumulationTime The minimal accumulation time; might <b>not</b> be
	 *                            &lt;0.
	 */
	public void setMinAccumulationTime(long minAccumulationTime) {
		if (minAccumulationTime < 0) {
			throw new IllegalArgumentException("Cannot set a minimal accumulation time that is <0");
		}
		this.minAccumulationTime = minAccumulationTime;
	}

	/**
	 * Sets the intervals in milliseconds the {@link MetricsSender} waits until it
	 * takes all accumulated {@link Metric}s and sends them as a
	 * {@link WebMetricRequest}.
	 * <p>
	 * If the sending fails more times than there are intervals set, the last
	 * defined interval is used.
	 * <p>
	 * For example, if the method is called with the arguments (0, 5000, 300000),
	 * the first retry will be done directly after the first failed, the second
	 * after 5 seconds and the third-&gt;nth after 5 minutes.
	 * <p>
	 * The default intervals are {@link #SENDING_RETRY_INTERVALS}.
	 * <p>
	 * Only has effect if the sender is operating on {@link SenderMode#PACKAGED}.
	 * 
	 * @param interval  The first interval; might <b>not</b> be negative.
	 * @param intervals The additional intervals; might <b>not</b> be negative.
	 */
	public void setSendingRetryIntervals(long interval, long... intervals) {
		long[] sendingRetryIntervals;
		if (intervals == null) {
			sendingRetryIntervals = new long[] { interval };
		} else {
			sendingRetryIntervals = new long[intervals.length + 1];
			Arrays.setAll(sendingRetryIntervals, i -> i == 0 ? interval : intervals[i - 1]);
		}
		if (Arrays.stream(sendingRetryIntervals).anyMatch(i -> i < 0)) {
			throw new IllegalArgumentException("Cannot set a retry interval < 0");
		}
		this.sendingRetryIntervals = sendingRetryIntervals;
	}

	/**
	 * Sets the count of retries that are performed if sending the
	 * {@link WebMetricRequest} via the wrapped {@link MetricsWebFacade} failed.
	 * <p>
	 * The amount of time between retries can be set using
	 * {@link #setSendingRetryIntervals(long, long...)}.
	 * <p>
	 * The default retry count is {@link #DEFAULT_MAX_RETRY_COUNT}.
	 * <p>
	 * Only has effect if the sender is operating on {@link SenderMode#PACKAGED}.
	 * 
	 * @param maxRetryCount The retry count; might not be negative.
	 */
	public void setMaxRetryCount(int maxRetryCount) {
		if (maxRetryCount < 0) {
			throw new IllegalArgumentException("Cannot set a retry count < 0");
		}
		this.maxRetryCount = maxRetryCount;
	}

	/**
	 * Returns whether the {@link MetricsSender} is currently locked
	 * <p>
	 * A locked sender can only occur if the sender is operating on
	 * {@link SenderMode#PACKAGED}.
	 * 
	 * @return True if this {@link MetricsSender} is locked, false otherwise
	 */
	public final boolean isLocked() {
		return this.locked;
	}

	/**
	 * Unlocks this {@link MetricsSender}, which will cause trying to send the
	 * accumulated {@link Metric}s to be triggered again.
	 * <p>
	 * Requires this {@link MetricsSender} to be locked, which can be checked using
	 * {@link #isLocked()}.
	 */
	public final void unlock() {
		this.sendLock.lock();
		if (!this.locked) {
			throw new IllegalStateException("Cannot unlock a non-locked sender");
		}
		this.locked = false;
		this.pack.transfer(0);
		this.sendLock.unlock();
	}

	@Override
	public void consume(String consumerId, UUID trailId, Metric metric) throws Exception {
		MetricValidator.validate(metric);
		WebMetric webMetric = WebMetric.from(metric);

		if (this.mode == SenderMode.SYNCHRONOUS) {
			WebMetricTrail webMetricTrail = new WebMetricTrail(trailId.toString(), webMetric);
			WebMetricConsumer webMetricConsumer = new WebMetricConsumer(consumerId, webMetricTrail);
			WebMetricRequest webMetricRequest = new WebMetricRequest(webMetricConsumer);

			this.facade.transfer(webMetricRequest);
		} else {
			try {
				this.packLock.lock();

				if (this.locked) {
					throw new IllegalStateException("The " + MetricsSender.class.getSimpleName()
							+ " is currently locked since the a readily packed "
							+ WebMetricRequest.class.getSimpleName()
							+ " could not be send; unable to consume more metrics in another "
							+ WebMetricRequest.class.getSimpleName());
				}

				this.pack.add(consumerId, trailId, webMetric);
			} finally {
				this.packLock.unlock();
			}
		}
	}

	/**
	 * Factory method for {@link MetricsSender}s.
	 * <p>
	 * Will use the given {@link MetricsWebFacade} to transfer packaged
	 * {@link Metric}s.
	 * 
	 * @param facade The facade that is able to transfer packaged {@link Metric}s
	 *               via web service when
	 *               {@link MetricsWebFacade#transfer(WebMetricRequest)}
	 *               is called; might <b>not</b> be null.
	 * @return A new {@link MetricsSender} instance, never null
	 */
	public static MetricsSender wrap(MetricsWebFacade facade) {
		return new MetricsSender(facade);
	}
}