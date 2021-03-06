/*
 * Copyright (c) Facebook, Inc. and its affiliates.
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

package com.facebook.buck.core.model;

import com.facebook.buck.core.cell.name.ImmutableCanonicalCellName;
import com.facebook.buck.core.model.impl.ImmutableUnconfiguredBuildTargetView;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.io.filesystem.impl.FakeProjectFilesystem;
import com.facebook.buck.support.cli.args.BuckCellArg;
import com.facebook.buck.util.stream.RichStream;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;
import java.nio.file.Path;
import javax.annotation.Nullable;

/** Allows to create {@link UnconfiguredBuildTargetView} during testing. */
public class UnconfiguredBuildTargetFactoryForTests {

  private UnconfiguredBuildTargetFactoryForTests() {}

  public static UnconfiguredBuildTargetView newInstance(
      ProjectFilesystem projectFilesystem, String fullyQualifiedName) {
    return newInstance(projectFilesystem.getRootPath(), fullyQualifiedName);
  }

  public static UnconfiguredBuildTargetView newInstance(String fullyQualifiedName) {
    return newInstance((Path) null, fullyQualifiedName);
  }

  public static UnconfiguredBuildTargetView newInstance(
      @Nullable Path root, String fullyQualifiedName) {
    root = root == null ? new FakeProjectFilesystem().getRootPath() : root;

    BuckCellArg arg = BuckCellArg.of(fullyQualifiedName);
    ImmutableCanonicalCellName cellName = ImmutableCanonicalCellName.of(arg.getCellName());
    String[] parts = arg.getBasePath().split(":");
    Preconditions.checkArgument(parts.length == 2);
    String[] nameAndFlavor = parts[1].split("#");
    if (nameAndFlavor.length != 2) {
      return ImmutableUnconfiguredBuildTargetView.of(
          UnflavoredBuildTarget.of(cellName, BaseName.of(parts[0]), parts[1]),
          UnconfiguredBuildTarget.NO_FLAVORS);
    }
    String[] flavors = nameAndFlavor[1].split(",");
    return ImmutableUnconfiguredBuildTargetView.of(
        UnflavoredBuildTarget.of(cellName, BaseName.of(parts[0]), nameAndFlavor[0]),
        RichStream.from(flavors)
            .map(InternalFlavor::of)
            .collect(
                ImmutableSortedSet.toImmutableSortedSet(UnconfiguredBuildTarget.FLAVOR_ORDERING)));
  }

  public static UnconfiguredBuildTargetView newInstance(String baseName, String shortName) {
    BuckCellArg arg = BuckCellArg.of(baseName);
    return ImmutableUnconfiguredBuildTargetView.of(
        UnflavoredBuildTarget.of(
            ImmutableCanonicalCellName.of(arg.getCellName()),
            BaseName.of(arg.getBasePath()),
            shortName),
        UnconfiguredBuildTarget.NO_FLAVORS);
  }

  public static UnconfiguredBuildTargetView newInstance(
      String baseName, String shortName, Flavor... flavors) {
    BuckCellArg arg = BuckCellArg.of(baseName);
    return ImmutableUnconfiguredBuildTargetView.of(
        UnflavoredBuildTarget.of(
            ImmutableCanonicalCellName.of(arg.getCellName()),
            BaseName.of(arg.getBasePath()),
            shortName),
        RichStream.from(flavors)
            .collect(
                ImmutableSortedSet.toImmutableSortedSet(UnconfiguredBuildTarget.FLAVOR_ORDERING)));
  }
}
