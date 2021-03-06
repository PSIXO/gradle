/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.execution.taskgraph;

import java.util.Set;

/*
 * Representing the result of he CalculateTaskGraph build operation.
 * This class is intentionally internal and consumed by the build scan plugin.
 *
 * @since 4.0
 */
public class CalculateTaskGraphOperationResult {
    private final Set<String> requestedTaskPaths;
    private final Set<String> filteredTaskPaths;

    public CalculateTaskGraphOperationResult(Set<String> requestedTaskPaths, Set<String> excludedTaskPaths) {
        this.requestedTaskPaths = requestedTaskPaths;
        this.filteredTaskPaths = excludedTaskPaths;
    }

    public Set<String> getRequestedTaskPaths() {
        return requestedTaskPaths;
    }

    public Set<String> getFilteredTaskPaths() {
        return filteredTaskPaths;
    }

}
