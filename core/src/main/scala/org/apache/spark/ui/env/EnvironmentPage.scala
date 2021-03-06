/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.ui.env

import javax.servlet.http.HttpServletRequest

import scala.xml.Node

import org.apache.spark.SparkConf
import org.apache.spark.status.AppStatusStore
import org.apache.spark.ui._
import org.apache.spark.util.Utils

private[ui] class EnvironmentPage(
    parent: EnvironmentTab,
    conf: SparkConf,
    store: AppStatusStore) extends WebUIPage("") {

  def render(request: HttpServletRequest): Seq[Node] = {
    val appEnv = store.environmentInfo()
    val jvmInformation = Map(
      "Java Version" -> appEnv.runtime.javaVersion,
      "Java Home" -> appEnv.runtime.javaHome,
      "Scala Version" -> appEnv.runtime.scalaVersion)

    val runtimeInformationTable = UIUtils.listingTable(
      propertyHeader, jvmRow, jvmInformation, fixedWidth = true)
    val sparkPropertiesTable = UIUtils.listingTable(propertyHeader, propertyRow,
      Utils.redact(conf, appEnv.sparkProperties.toSeq), fixedWidth = true)
    val systemPropertiesTable = UIUtils.listingTable(
      propertyHeader, propertyRow, appEnv.systemProperties, fixedWidth = true)
    val classpathEntriesTable = UIUtils.listingTable(
      classPathHeaders, classPathRow, appEnv.classpathEntries, fixedWidth = true)
    val content =
      <span>
        <h4>Runtime Information</h4> {runtimeInformationTable}
        <h4>Spark Properties</h4> {sparkPropertiesTable}
        <h4>System Properties</h4> {systemPropertiesTable}
        <h4>Classpath Entries</h4> {classpathEntriesTable}
      </span>

    UIUtils.headerSparkPage("Environment", content, parent)
  }

  private def propertyHeader = Seq("Name", "Value")
  private def classPathHeaders = Seq("Resource", "Source")
  private def jvmRow(kv: (String, String)) = <tr><td>{kv._1}</td><td>{kv._2}</td></tr>
  private def propertyRow(kv: (String, String)) = <tr><td>{kv._1}</td><td>{kv._2}</td></tr>
  private def classPathRow(data: (String, String)) = <tr><td>{data._1}</td><td>{data._2}</td></tr>
}

private[ui] class EnvironmentTab(
    parent: SparkUI,
    store: AppStatusStore) extends SparkUITab(parent, "environment") {
  attachPage(new EnvironmentPage(this, parent.conf, store))
}
