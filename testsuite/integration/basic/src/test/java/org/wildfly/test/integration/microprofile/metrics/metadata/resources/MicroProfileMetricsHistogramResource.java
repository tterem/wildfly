/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.test.integration.microprofile.metrics.metadata.resources;

import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/histogram")
public class MicroProfileMetricsHistogramResource {

   @Inject
   MetricRegistry registry;

   @GET
   @Path("/hello/{n}")
   public Response hello(@PathParam("n") String n) {
      Metadata histogramMetadata = new Metadata("helloHistogram", MetricType.HISTOGRAM);

      // TODO: Remove following line once https://github.com/smallrye/smallrye-metrics/issues/42 is fixed
      histogramMetadata.setReusable(true); // workaround

      Histogram histogram = registry.histogram(histogramMetadata);
      histogram.update(Long.valueOf(n));
      return Response.ok("Hello World!").build();
   }

}
