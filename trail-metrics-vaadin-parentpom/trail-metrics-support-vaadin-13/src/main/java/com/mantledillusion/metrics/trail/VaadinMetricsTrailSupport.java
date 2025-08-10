package com.mantledillusion.metrics.trail;

import com.mantledillusion.essentials.object.Null;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * {@link MetricsTrail} support for Vaadin 13.
 * <p>
 * By supporting a {@link VaadinService} using {@link #support(VaadinService)}, the {@link VaadinMetricsTrailSupport}
 * will automatically begin new {@link MetricsTrail}s for every new {@link VaadinSession} of that service.
 */
public class VaadinMetricsTrailSupport extends AbstractVaadinMetricsTrailSupport<VaadinService, VaadinSession> {

    private VaadinMetricsTrailSupport(VaadinService service) {
        super(service);
    }

    @Override
    protected void observe(VaadinService service, Consumer<VaadinSession> sessionInitListener,
                           BiConsumer<String, String> urlListener, Consumer<VaadinSession> sessionDestroyListener) {
        service.addSessionInitListener(event -> sessionInitListener.accept(event.getSession()));
        service.addUIInitListener(uiInitEvent -> uiInitEvent.getUI().addBeforeLeaveListener(beforeLeaveEvent -> {
            Location location = beforeLeaveEvent.getLocation();
            urlListener.accept(location.getPath(), location.getQueryParameters().getQueryString());
        }));
        service.addSessionDestroyListener(event -> sessionDestroyListener.accept(event.getSession()));
    }

    @Override
    protected void hookSession(VaadinSession session, MetricsTrail trail) {
        session.setAttribute(MetricsTrail.class, trail);
    }

    @Override
    protected String getSessionId(VaadinSession session) {
        return session.getSession().getId();
    }

    @Override
    protected BrowserInfo getSessionBrowserInfo(VaadinSession session) {
        WebBrowser browser = session.getBrowser();
        String application = browser.getBrowserApplication();
        BrowserInfo.BrowserType browserType = BrowserInfo.BrowserType.of(browser.isChrome(),
                browser.isEdge(), browser.isFirefox(), browser.isIE(), browser.isOpera(), browser.isSafari());
        String browserVersion = browser.getBrowserMajorVersion() + "." + browser.getBrowserMinorVersion();
        BrowserInfo.SystemEnvironmentType environmentType = BrowserInfo.SystemEnvironmentType.of(
                Null.get(browser::isAndroid, false), Null.get(browser::isIPad, false),
                Null.get(browser::isIPhone, false), Null.get(browser::isLinux, false),
                Null.get(browser::isMacOSX, false), Null.get(browser::isWindows, false),
                Null.get(browser::isWindowsPhone, false));
        return new BrowserInfo(application, browserType, browserVersion, environmentType);
    }

    @Override
    protected MetricsTrail getSessionTrail() {
        return getCurrent();
    }

    /**
     * Returns the {@link MetricsTrail} of the current {@link VaadinSession}.
     *
     * @return The current {@link Thread} {@link VaadinSession}'s {@link MetricsTrail}, never null
     * @throws IllegalStateException If the current {@link Thread} does not have a {@link VaadinSession}
     */
    public static MetricsTrail getCurrent() throws IllegalStateException {
        if (VaadinSession.getCurrent() == null) {
            throw new IllegalStateException("There is no VaadinSession in the current thread");
        }
        return VaadinSession.getCurrent().getAttribute(MetricsTrail.class);
    }

    /**
     * Permits {@link VaadinMetricsTrailSupport} to enable {@link MetricsTrail} support for the given {@link VaadinService}.
     * <p>
     * The returned {@link VaadinMetricsTrailSupport} can be used to hook {@link MetricsTrailConsumer}s via
     * {@link #hook(MetricsTrailConsumer)}.
     * <p>
     * Will make the following {@link GeneralVaadinMetrics} available to all {@link MetricsConsumer}s:<br>
     * - {@link GeneralVaadinMetrics#SESSION_BEGIN}<br>
     * - {@link GeneralVaadinMetrics#SESSION_END}<br>
     * - {@link GeneralVaadinMetrics#BROWSER_INFO}<br>
     * - {@link GeneralVaadinMetrics#NAVIGATION}<br>
     *
     * @param service The {@link VaadinService} to observe; might <b>not</b> be null.
     * @return A new {@link VaadinMetricsTrailSupport} instance, never null
     */
    public static VaadinMetricsTrailSupport support(VaadinService service) {
        if (service == null) {
            throw new IllegalArgumentException("Cannot observe a null service");
        }
        return new VaadinMetricsTrailSupport(service);
    }

    /**
     * Permits {@link VaadinMetricsTrailSupport}s to enable {@link MetricsTrail} support for the given {@link ErrorHandler}.
     * <p>
     * For observation, the already existing {@link VaadinMetricsTrailSupport} of the {@link VaadinSession} during the
     * occuring error is used.
     * <p>
     * The wrapping handler will dispatch metrics for all errors given to it before delegating them to its wrapped
     * {@link ErrorHandler}.
     * <p>
     * Will make the following {@link GeneralVaadinMetrics} available to all {@link MetricsConsumer}s:<br>
     * - {@link GeneralVaadinMetrics#ERROR}<br>
     *
     * @param errorHandler The {@link ErrorHandler} instance to delegate to after dispatching; might <b>not</b> be null.
     * @return A new {@link MetricsWrappingErrorHandler}, never null
     */
    public static MetricsWrappingErrorHandler support(ErrorHandler errorHandler) {
        if (errorHandler == null) {
            throw new IllegalArgumentException("Cannot wrap a null error handler");
        }
        return new MetricsWrappingErrorHandler(errorHandler, () -> getCurrent());
    }
}
