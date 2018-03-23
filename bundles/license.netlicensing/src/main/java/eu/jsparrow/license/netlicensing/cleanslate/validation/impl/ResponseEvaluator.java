package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;

import java.time.ZonedDateTime;
import java.util.List;

import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.Floating;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.MultiFeature;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.Subscription;


public class ResponseEvaluator {
	
	public LicenseValidationResult evaluate(List<Subscription> subscriptions, List<MultiFeature> multiFeatures, List<Floating> floatings) {
		
		Subscription subscription = subscriptions.get(0);
		MultiFeature multiFeature = multiFeatures.get(0);
		Floating floating = floatings.get(0);
		
		if(subscription.isValid()) {
			if(multiFeature.isValid()) {
				/*
				 * Valid NodeLocked
				 */
			} else if (floating.isValid()) {
				/*
				 * Valid Floating
				 */
			} else {
				/*
				 * Floating out of sessions
				 */
			}
		} else {
			ZonedDateTime subscriptionExpires = subscription.getExpires();
			if(subscriptionExpires.isAfter(ZonedDateTime.now())) {
				/*
				 * HW ID mismatch is the only explanation.
				 * TODO: there is a flag in the validationResponse for that as well
				 */
			} else if(multiFeature.isValid()) {
				/*
				 * NodeLocked expired
				 */
			} else if(floating.isValid()) {
				/*
				 * Floating expired
				 */
			}
		}
		
		return null;
	}
	
	public LicenseValidationResult evaluate(List<Subscription> subscriptions, List<MultiFeature> multiFeatures) {
		
		Subscription subscription = subscriptions.get(0);
		MultiFeature multiFeature = multiFeatures.get(0);
		
		if(subscription.isValid()) {
			if(multiFeature.isValid()) {
				/*
				 * Valid NodeLocked
				 */
			} else {
				/*
				 * This should not happen 
				 */
			}
		} else {
			ZonedDateTime subscriptionExpires = subscription.getExpires();
			if(subscriptionExpires.isAfter(ZonedDateTime.now())) {
				/*
				 * HW ID mismatch is the only explanation.
				 * TODO: there is a flag in the validationResponse for that as well
				 */
			} else {
				/*
				 * This should not happen
				 */
			}
		}
		
		return null;
	}
	
	protected Subscription getLatestSubscription(List<Subscription>subscriptions) {
		return null;
	}
	
	protected Floating getValidFloating(List<Floating>floatings) {
		return null;
	}
	
	protected MultiFeature getValidMultifeature(List<MultiFeature>multiFeatures) {
		return null;
	}

}
