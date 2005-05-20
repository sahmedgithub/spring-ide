/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.web.flow.core.model;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.web.flow.core.WebFlowDefinitionException;

/**
 * This interface provides information for a Spring web flow configuration.
 */
public interface IWebFlowConfig extends IWebFlowModelElement {

    IFile getConfigFile();

    String getConfigPath();

    WebFlowDefinitionException getException();

    IWebFlowState getState();

    IWebFlowState getState(boolean reload);

    void setState(IWebFlowState state);
}