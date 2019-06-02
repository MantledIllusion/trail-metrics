package com.mantledillusion.metrics.trail;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;

/**
 * Validator for {@link Metric} instances.
 */
public class MetricValidator {

	private MetricValidator() {
	}

	private static final Predicate<MetricAttribute> NULL_ATTRIBUTE_PREDICATE = attribute ->
			attribute == null || attribute.getKey() == null || attribute.getValue() == null;

	private static final Predicate<MetricAttribute> NUMERIC_OPERATOR_PREDICATE = attribute -> {
		if (Metric.OPERATOR_ATTRIBUTE_KEY.equals(attribute.getKey())) {
			try {
				Float.parseFloat(attribute.getValue());
				return true;
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"The attribute '" + Metric.OPERATOR_ATTRIBUTE_KEY + "' needs to be a Float numeric");
			}
		}
		return false;
	};

	public static final Consumer<Metric> VALIDATOR_NOOP = metric -> {
	};
	public static final Consumer<Metric> VALIDATOR_NUMERIC_OPERATOR = metric -> {
		if (metric.getAttributes() == null || metric.getAttributes().stream().noneMatch(NUMERIC_OPERATOR_PREDICATE)) {
			throw new IllegalArgumentException("Cannot dispatch a " + metric.getType().name() + " metric without a '"
					+ Metric.OPERATOR_ATTRIBUTE_KEY + "' attribute");
		}
	};

	/**
	 * Validates that the given {@link Metric} is valid to be dispatched:<br>
	 * - Has to have a timestamp<br>
	 * - Has to have a non-empty identifier<br>
	 * - Has to have a type<br>
	 * - If it has attributes, they all have to be non-null with non-empty key and
	 * non-null value<br>
	 * - If the type requires a specific {@link Metric#OPERATOR_ATTRIBUTE_KEY}
	 * attribute, it has to exist and its value has to fulfill the type's
	 * requirements<br>
	 * 
	 * @param metric The metric to validate; might be null.
	 * @throws IllegalArgumentException If the metric is invalid in any way.
	 */
	public static void validate(Metric metric) throws IllegalArgumentException {
		if (metric == null) {
			throw new IllegalArgumentException("Cannot dispatch a null metric");
		} else if (metric.getTimestamp() == null) {
			throw new IllegalArgumentException("Cannot dispatch a timestampless metric");
		} else if (metric.getIdentifier() == null || metric.getIdentifier().isEmpty()) {
			throw new IllegalArgumentException("Cannot dispatch an identifierless metric");
		} else if (metric.getType() == null) {
			throw new IllegalArgumentException("Cannot dispatch an typeless metric");
		} else if (metric.getAttributes() != null
				&& metric.getAttributes().stream().anyMatch(NULL_ATTRIBUTE_PREDICATE)) {
			throw new IllegalArgumentException(
					"Cannot dispatch a metric with an attribute that is either null or contains a null key/value");
		}

		metric.getType().validateOperator(metric);
	}
}
