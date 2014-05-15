package com.criteo.kafka;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;

public class RegexMetricPredicate implements MetricPredicate {

	Pattern pattern = null;
	//static Logger LOG = Logger.getLogger(RegexMetricPredicate.class);
	
	public RegexMetricPredicate(String regex) {
		pattern = Pattern.compile(regex);
	}
	
	@Override
	public boolean matches(MetricName name, Metric metric) {
		boolean ok = !pattern.matcher(name.getName()).matches();
		//LOG.info(String.format("name: %s - %s", name.getName(), ok));
		return ok;
	}

}
