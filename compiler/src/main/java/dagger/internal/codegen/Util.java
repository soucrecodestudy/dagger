/*
 * Copyright (C) 2013 Google, Inc.
 * Copyright (C) 2013 Square, Inc.
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
package dagger.internal.codegen;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static com.google.auto.common.MoreElements.getLocalAndInheritedMethods;
import static com.google.auto.common.MoreElements.hasModifiers;
import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Utilities for handling types in annotation processors
 */
final class Util {

  /**
   * Returns true if and only if a component can instantiate new instances (typically of a module)
   * rather than requiring that they be passed.
   */
  static boolean componentCanMakeNewInstances(TypeElement typeElement) {
    switch (typeElement.getKind()) {
      case CLASS:
        break;
      case ENUM:
      case ANNOTATION_TYPE:
      case INTERFACE:
        return false;
      default:
        throw new AssertionError("TypeElement cannot have kind: " + typeElement.getKind());
    }

    if (typeElement.getModifiers().contains(ABSTRACT)) {
      return false;
    }

    if (requiresEnclosingInstance(typeElement)) {
      return false;
    }

    for (Element enclosed : typeElement.getEnclosedElements()) {
      if (enclosed.getKind().equals(CONSTRUCTOR)
          && ((ExecutableElement) enclosed).getParameters().isEmpty()
          && !enclosed.getModifiers().contains(PRIVATE)) {
        return true;
      }
    }

    // TODO(gak): still need checks for visibility

    return false;
  }

  private static boolean requiresEnclosingInstance(TypeElement typeElement) {
    switch (typeElement.getNestingKind()) {
      case TOP_LEVEL:
        return false;
      case MEMBER:
        return !typeElement.getModifiers().contains(STATIC);
      case ANONYMOUS:
      case LOCAL:
        return true;
      default:
        throw new AssertionError(
            "TypeElement cannot have nesting kind: " + typeElement.getNestingKind());
    }
  }

  static ImmutableSet<ExecutableElement> getUnimplementedMethods(
      Elements elements, TypeElement type) {
    return FluentIterable.from(getLocalAndInheritedMethods(type, elements))
        .filter(hasModifiers(ABSTRACT))
        .toSet();
  }

  private Util() {}
}
