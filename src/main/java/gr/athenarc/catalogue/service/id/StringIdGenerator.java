/*
 * Copyright 2021-2024 OpenAIRE AMKE
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

package gr.athenarc.catalogue.service.id;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Service
public class StringIdGenerator implements IdGenerator<String> {

    public static final int DEFAULT_LENGTH = 8;

    @Override
    public String createId() {
        return createId("", DEFAULT_LENGTH);
    }

    @Override
    public String createId(String prefix) {
        return createId(prefix, DEFAULT_LENGTH);
    }

    @Override
    public String createId(String prefix, int length) {
        if (prefix == null) {
            prefix = "";
        }
        return prefix + RandomStringUtils.randomAlphabetic(length);
    }
}
