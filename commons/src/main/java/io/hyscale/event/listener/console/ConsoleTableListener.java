/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.event.listener.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.hyscale.commons.logger.TableFormatter;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.event.model.InformationEvent;

@Component
public class ConsoleTableListener implements ApplicationListener<InformationEvent<TableFormatter>> {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleTableListener.class);

    @Override
    public void onApplicationEvent(InformationEvent<TableFormatter> event) {
        logger.debug("{}: Listening information event: {}", this.getClass().getName(), event);
        WorkflowLogger.logTable(event.getMessage());
    }

}
