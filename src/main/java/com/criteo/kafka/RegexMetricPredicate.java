/*
 *  Copyright 2014 Damien Claveau
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */


package com.criteo.kafka;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;

public class RegexMetricPredicate implements MetricPredicate {

	Pattern pattern = null;
	static Logger LOG = Logger.getLogger(RegexMetricPredicate.class);
	
	public RegexMetricPredicate(String regex) {
		pattern = Pattern.compile(regex);
	}
	
	@Override
	public boolean matches(MetricName name, Metric metric) {
		boolean ok = !pattern.matcher(String.format("%s.%s.%s", name.getGroup(), name.getType(), name.getName())).matches();
		// LOG.warn(String.format("name: %s.%s.%s - %s", name.getGroup(), name.getType(), name.getName(), ok));
		return ok;
	}

}
