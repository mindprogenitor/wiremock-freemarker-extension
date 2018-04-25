/*
 * FreemarkerVariableRepository.java, 22 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mindprogeny.wiremock.extension.freemarker;

import com.github.tomakehurst.wiremock.admin.Router;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.mindprogeny.wiremock.extension.freemarker.extension.variable.task.ClearVariables;
import com.mindprogeny.wiremock.extension.freemarker.extension.variable.task.GetVariableSet;
import com.mindprogeny.wiremock.extension.freemarker.extension.variable.task.GetVariableSets;
import com.mindprogeny.wiremock.extension.freemarker.extension.variable.task.RemoveVariableSet;
import com.mindprogeny.wiremock.extension.freemarker.extension.variable.task.SetVariableSet;
import com.mindprogeny.wiremock.extension.freemarker.extension.variable.task.SetVariableSets;

import static com.github.tomakehurst.wiremock.http.RequestMethod.*;

/**
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 22 Mar 2018
 *
 */
public class FreemarkerVariableRepositoryManager implements AdminApiExtension {

    /**
     * @see com.github.tomakehurst.wiremock.extension.Extension#getName()
     */
    @Override
    public String getName() {
        return "variable-rep-manager";
    }

    /**
     * @see com.github.tomakehurst.wiremock.extension.AdminApiExtension#contributeAdminApiRoutes(com.github.tomakehurst.wiremock.admin.Router)
     */
    @Override
    public void contributeAdminApiRoutes(Router router) {
        router.add(DELETE, "/variables/remove", ClearVariables.class);
        router.add(DELETE, "/variables/remove/{set}", RemoveVariableSet.class);
        router.add(GET, "/variables", GetVariableSets.class);
        router.add(GET, "/variables/{set}", GetVariableSet.class);
        router.add(POST, "/variables", SetVariableSets.class);
        router.add(POST, "/variables/{set}", SetVariableSet.class);
    }


}
