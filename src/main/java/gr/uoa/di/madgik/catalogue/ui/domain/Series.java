/**
 * Copyright 2021-2025 OpenAIRE AMKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.uoa.di.madgik.catalogue.ui.domain;

public class Series {

    String name;
    String referenceYear;

    public Series() {
    }

    public String getName() {
        return name;
    }

    public Series setName(String name) {
        this.name = name;
        return this;
    }

    public String getReferenceYear() {
        return referenceYear;
    }

    public Series setReferenceYear(String referenceYear) {
        this.referenceYear = referenceYear;
        return this;
    }
}
