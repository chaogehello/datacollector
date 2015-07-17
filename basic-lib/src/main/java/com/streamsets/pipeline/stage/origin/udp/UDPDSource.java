/**
 * (c) 2015 StreamSets, Inc. All rights reserved. May not
 * be copied, modified, or distributed in whole or part without
 * written consent of StreamSets, Inc.
 */
package com.streamsets.pipeline.stage.origin.udp;

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ConfigGroups;
import com.streamsets.pipeline.api.ExecutionMode;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.Source;
import com.streamsets.pipeline.api.StageDef;
import com.streamsets.pipeline.api.ValueChooser;
import com.streamsets.pipeline.api.impl.Utils;
import com.streamsets.pipeline.config.CharsetChooserValues;
import com.streamsets.pipeline.configurablestage.DSource;
import com.streamsets.pipeline.lib.parser.ParserConfig;

import java.util.List;

import static com.streamsets.pipeline.lib.parser.ParserConfigKey.AUTH_FILE_PATH;
import static com.streamsets.pipeline.lib.parser.ParserConfigKey.CHARSET;
import static com.streamsets.pipeline.lib.parser.ParserConfigKey.CONVERT_TIME;
import static com.streamsets.pipeline.lib.parser.ParserConfigKey.EXCLUDE_INTERVAL;
import static com.streamsets.pipeline.lib.parser.ParserConfigKey.TYPES_DB_PATH;

@StageDef(
    version = 1,
    label = "UDP Source",
    description = "Listens for UDP messages on a single port",
    icon = "udp.png",
    execution = ExecutionMode.STANDALONE,
    recordsByRef = true
)

@ConfigGroups(Groups.class)
@GenerateResourceBundle
public class UDPDSource extends DSource {
  private ParserConfig parserConfig = new ParserConfig();

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.LIST,
      label = "Port",
      defaultValue = "[\"9995\"]",
      description = "Port to listen on",
      group = "UDP",
      displayPosition = 10
  )
  public List<String> ports; // string so we can listen on multiple ports in the future

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      label = "Data Format",
      defaultValue = "SYSLOG",
      group = "UDP",
      displayPosition = 20
  )
  @ValueChooser(UDPDataFormatChooserValues.class)
  public UDPDataFormat dataFormat;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.NUMBER,
      defaultValue = "1000",
      label = "Max Batch Size (messages)",
      group = "UDP",
      displayPosition = 30,
      min = 0,
      max = Integer.MAX_VALUE
  )
  public int batchSize;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.NUMBER,
      defaultValue = "1000",
      label = "Batch Wait Time (ms)",
      description = "Max time to wait for data before sending a batch",
      displayPosition = 40,
      group = "UDP",
      min = 1,
      max = Integer.MAX_VALUE
  )
  public int maxWaitTime;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue = "UTF-8",
      label = "Charset",
      displayPosition = 5,
      group = "SYSLOG",
      dependsOn = "dataFormat",
      triggeredByValue = "SYSLOG"
  )
  @ValueChooser(CharsetChooserValues.class)
  public String syslogCharset;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.STRING,
      label = "TypesDB File Path",
      description = "User-specified TypesDB file. Overrides the included version.",
      displayPosition = 10,
      group = "COLLECTD",
      dependsOn = "dataFormat",
      triggeredByValue = "COLLECTD"
  )
  public String typesDbPath;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.BOOLEAN,
      defaultValue = "false",
      label = "Convert Hi-res Time & Interval",
      description = "Converts high resolution time format interval and timestamp to unix time in (ms).",
      displayPosition = 20,
      group = "COLLECTD",
      dependsOn = "dataFormat",
      triggeredByValue = "COLLECTD"
  )
  public boolean convertTime;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.BOOLEAN,
      defaultValue = "true",
      label = "Exclude Interval",
      description = "Excludes the interval field from output records.",
      displayPosition = 30,
      group = "COLLECTD",
      dependsOn = "dataFormat",
      triggeredByValue = "COLLECTD"
  )
  public boolean excludeInterval;

  @ConfigDef(
      required = false,
      type = ConfigDef.Type.STRING,
      label = "Auth File",
      description = "",
      displayPosition = 40,
      group = "COLLECTD",
      dependsOn = "dataFormat",
      triggeredByValue = "COLLECTD"
  )
  public String authFilePath;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue = "UTF-8",
      label = "Charset",
      displayPosition = 50,
      group = "SYSLOG",
      dependsOn = "dataFormat",
      triggeredByValue = "COLLECTD"
  )
  @ValueChooser(CharsetChooserValues.class)
  public String collectdCharset;

  @Override
  protected Source createSource() {
    Utils.checkNotNull(dataFormat, "Data format cannot be null");
    Utils.checkNotNull(ports, "Ports cannot be null");

    switch (dataFormat) {
      case SYSLOG:
        parserConfig.put(CHARSET, syslogCharset);
        break;
      case COLLECTD:
        parserConfig.put(CHARSET, collectdCharset);
        break;
      default:
        // NOOP
    }

    parserConfig.put(CONVERT_TIME, convertTime);
    parserConfig.put(TYPES_DB_PATH, typesDbPath);
    parserConfig.put(EXCLUDE_INTERVAL, excludeInterval);
    parserConfig.put(AUTH_FILE_PATH, authFilePath);
    return new UDPSource(ports, parserConfig, dataFormat, batchSize, maxWaitTime);
  }
}
