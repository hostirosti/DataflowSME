/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.dataflow.tutorials.game.utils;

import com.google.cloud.dataflow.sdk.transforms.Aggregator;
import com.google.cloud.dataflow.sdk.transforms.DoFn;
import com.google.cloud.dataflow.sdk.transforms.Sum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses the raw game event info into GameEvent objects. Each event line has the following
 * format: username,teamname,score,timestamp_in_ms,readable_time,event_id
 * e.g.:
 *   user2_AsparagusPig,AsparagusPig,10,1445230923951,
 *   2015-11-02 09:09:28.224,e8018d7d-18a6-4265-ba7e-55666b898b6f
 * The human-readable time string is not used here.
 */
public class ParseEventFn extends DoFn<String, GameEvent> {

  // Log and count parse errors.
  private static final Logger LOG = LoggerFactory.getLogger(ParseEventFn.class);
  private final Aggregator<Long, Long> numParseErrors =
      createAggregator("ParseErrors", new Sum.SumLongFn());

  @Override
  public void processElement(ProcessContext c) {
    String[] components = c.element().split(",");
    try {
      String user = components[0].trim();
      String team = components[1].trim();
      Integer score = Integer.parseInt(components[2].trim());
      Long timestamp = Long.parseLong(components[3].trim());
      String eventId = components[5].trim();
      GameEvent gInfo = new GameEvent(user, team, score, timestamp, eventId);
      c.output(gInfo);
    } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
      numParseErrors.addValue(1L);
      LOG.info("Parse error on " + c.element() + ", " + e.getMessage());
    }
  }
}