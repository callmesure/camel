/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.jira.producer;

import java.io.File;
import java.net.URI;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import org.apache.camel.Exchange;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.jira.JiraEndpoint;
import org.apache.camel.support.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.component.jira.JiraConstants.ISSUE_KEY;

public class AttachFileProducer extends DefaultProducer {

    private static final transient Logger LOG = LoggerFactory.getLogger(AttachFileProducer.class);

    public AttachFileProducer(JiraEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    public void process(Exchange exchange) {
        String issueKey = exchange.getIn().getHeader(ISSUE_KEY, String.class);
        if (issueKey == null) {
            throw new IllegalArgumentException("Missing exchange input header named \'IssueKey\', it should specify the issue key to attach a file.");
        }
        Object body = exchange.getIn().getBody();
        if (body instanceof GenericFile) {
            JiraRestClient client = ((JiraEndpoint) getEndpoint()).getClient();
            IssueRestClient issueClient = client.getIssueClient();
            Issue issue = issueClient.getIssue(issueKey).claim();
            URI attachmentsUri = issue.getAttachmentsUri();
            GenericFile<File> file = (GenericFile<File>) body;
            issueClient.addAttachments(attachmentsUri, file.getFile());
        } else {
            LOG.info("Jira AttachFileProducer can attach only one file on each invocation. The body instance is " + body.getClass().getName() + " but it accepts only GenericFile<File>. You can "
                + "write a rooute processor to transform any incoming file to a Generic<File>");
        }
    }
}
