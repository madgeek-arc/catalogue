/**
 * Copyright 2021-2025 OpenAIRE AMKE
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gr.uoa.di.madgik.catalogue.aspects;

import gr.uoa.di.madgik.catalogue.service.ModelResponseValidator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * Aspect validating the given resources before adding/updating them in the catalogue.
 */
@Aspect
@Component
public class GenericResourceValidationAspect {

    private final ModelResponseValidator validator;

    GenericResourceValidationAspect(ModelResponseValidator validator) {
        this.validator = validator;
    }

    @Before("execution(* gr.uoa.di.madgik.catalogue.service.GenericResourceManager.add(..))")
    public void validateBeforeAdd(JoinPoint joinPoint) {
        String resourceTypeName = (String) joinPoint.getArgs()[0];
        Object item = joinPoint.getArgs()[1];
        validator.validate(item, resourceTypeName);
    }

    @Before("execution(* gr.uoa.di.madgik.catalogue.service.GenericResourceManager.update(..))")
    public void validateBeforeUpdate(JoinPoint joinPoint) {
        String resourceTypeName = (String) joinPoint.getArgs()[0];
        Object item = joinPoint.getArgs()[2];
        validator.validate(item, resourceTypeName);
    }
}
