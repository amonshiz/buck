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
import com.facebook.buck.support.cli.args.BuckCellArg;
import com.facebook.buck.util.stream.RichStream;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;

/**
 * Exposes some {@link com.facebook.buck.core.model.BuildTarget} logic that is only visible for
 * testing.
 */
public class BuildTargetFactory {

  private BuildTargetFactory() {
    // Utility class
  }

  public static BuildTarget newInstance(String fullyQualifiedName) {
    return newInstance(fullyQualifiedName, UnconfiguredTargetConfiguration.INSTANCE);
  }

  public static BuildTarget newInstance(
      String fullyQualifiedName, TargetConfiguration targetConfiguration) {
    BuckCellArg arg = BuckCellArg.of(fullyQualifiedName);
    ImmutableCanonicalCellName cellName = ImmutableCanonicalCellName.of(arg.getCellName());
    String[] parts = arg.getBasePath().split(":");
    Preconditions.checkArgument(parts.length == 2);
    String[] nameAndFlavor = parts[1].split("#");
    if (nameAndFlavor.length != 2) {
      return ImmutableUnconfiguredBuildTargetView.of(
              UnconfiguredBuildTarget.of(
                  UnflavoredBuildTarget.of(cellName, BaseName.of(parts[0]), parts[1]),
                  UnconfiguredBuildTarget.NO_FLAVORS))
          .configure(targetConfiguration);
    }
    return ImmutableUnconfiguredBuildTargetView.of(
            UnflavoredBuildTarget.of(cellName, BaseName.of(parts[0]), nameAndFlavor[0]),
            splitFlavors(nameAndFlavor[1]))
        .configure(targetConfiguration);
  }

  private static ImmutableSortedSet<Flavor> splitFlavors(String flavors) {
    return RichStream.from(flavors.split(","))
        .map(InternalFlavor::of)
        .collect(ImmutableSortedSet.toImmutableSortedSet(UnconfiguredBuildTarget.FLAVOR_ORDERING));
  }

  public static BuildTarget newInstance(String baseName, String shortName) {
    BuckCellArg arg = BuckCellArg.of(baseName);
    return ImmutableUnconfiguredBuildTargetView.of(
            UnflavoredBuildTarget.of(
                ImmutableCanonicalCellName.of(arg.getCellName()),
                BaseName.of(arg.getBasePath()),
                shortName),
            UnconfiguredBuildTarget.NO_FLAVORS)
        .configure(UnconfiguredTargetConfiguration.INSTANCE);
  }

  public static BuildTarget newInstance(String baseName, String shortName, Flavor... flavors) {
    BuckCellArg arg = BuckCellArg.of(baseName);
    return ImmutableUnconfiguredBuildTargetView.of(
            UnflavoredBuildTarget.of(
                ImmutableCanonicalCellName.of(arg.getCellName()),
                BaseName.of(arg.getBasePath()),
                shortName),
            RichStream.from(flavors)
                .collect(
                    ImmutableSortedSet.toImmutableSortedSet(
                        UnconfiguredBuildTarget.FLAVOR_ORDERING)))
        .configure(UnconfiguredTargetConfiguration.INSTANCE);
  }
}
