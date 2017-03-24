/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.api.internal.artifacts.transform;

import com.google.common.collect.Lists;
import org.gradle.api.Action;
import org.gradle.api.ActionConfiguration;
import org.gradle.api.artifacts.transform.ArtifactTransform;
import org.gradle.api.artifacts.transform.VariantTransform;
import org.gradle.api.artifacts.transform.VariantTransformConfigurationException;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.internal.DefaultActionConfiguration;
import org.gradle.api.internal.InstantiatorFactory;
import org.gradle.api.internal.artifacts.VariantTransformRegistry;
import org.gradle.api.internal.attributes.AttributeContainerInternal;
import org.gradle.api.internal.attributes.DefaultMutableAttributeContainer;
import org.gradle.api.internal.attributes.ImmutableAttributes;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.changedetection.state.GenericFileCollectionSnapshotter;
import org.gradle.api.internal.changedetection.state.ValueSnapshotter;
import org.gradle.internal.classloader.ClassLoaderHierarchyHasher;

import java.util.List;

public class DefaultVariantTransformRegistry implements VariantTransformRegistry {
    private static final Object[] NO_PARAMETERS = new Object[0];
    private final List<Registration> transforms = Lists.newArrayList();
    private final ImmutableAttributesFactory immutableAttributesFactory;
    private final GenericFileCollectionSnapshotter fileCollectionSnapshotter;
    private final TransformedFileCache transformedFileCache;
    private final ValueSnapshotter valueSnapshotter;
    private final ClassLoaderHierarchyHasher classLoaderHierarchyHasher;
    private final InstantiatorFactory instantiatorFactory;

    public DefaultVariantTransformRegistry(InstantiatorFactory instantiatorFactory, ImmutableAttributesFactory immutableAttributesFactory, TransformedFileCache transformedFileCache, GenericFileCollectionSnapshotter fileCollectionSnapshotter, ValueSnapshotter valueSnapshotter, ClassLoaderHierarchyHasher classLoaderHierarchyHasher) {
        this.instantiatorFactory = instantiatorFactory;
        this.immutableAttributesFactory = immutableAttributesFactory;
        this.fileCollectionSnapshotter = fileCollectionSnapshotter;
        this.transformedFileCache = transformedFileCache;
        this.valueSnapshotter = valueSnapshotter;
        this.classLoaderHierarchyHasher = classLoaderHierarchyHasher;
    }

    @Override
    public void registerTransform(Action<? super VariantTransform> registrationAction) {
        RecordingRegistration reg = instantiatorFactory.decorate().newInstance(RecordingRegistration.class, immutableAttributesFactory);
        registrationAction.execute(reg);
        if (reg.type == null) {
            throw new VariantTransformConfigurationException("Could not register transform: ArtifactTransform must be provided for registration.");
        }

        // TODO - should calculate this lazily
        Object[] parameters = getTransformParameters(reg.config);

        Registration registration = new DefaultVariantTransformRegistration(ImmutableAttributes.of(reg.from), ImmutableAttributes.of(reg.to), reg.type, parameters,  transformedFileCache, fileCollectionSnapshotter, valueSnapshotter, classLoaderHierarchyHasher, instantiatorFactory.inject());
        transforms.add(registration);
    }

    public Iterable<Registration> getTransforms() {
        return transforms;
    }

    private Object[] getTransformParameters(Action<? super ActionConfiguration> configAction) {
        if (configAction == null) {
            return NO_PARAMETERS;
        }
        ActionConfiguration config = new DefaultActionConfiguration();
        configAction.execute(config);
        return config.getParams();
    }

    public static class RecordingRegistration implements VariantTransform {
        final AttributeContainerInternal from;
        final AttributeContainerInternal to;
        private Class<? extends ArtifactTransform> type;
        private Action<? super ActionConfiguration> config;

        public RecordingRegistration(ImmutableAttributesFactory immutableAttributesFactory) {
            from = new DefaultMutableAttributeContainer(immutableAttributesFactory);
            to = new DefaultMutableAttributeContainer(immutableAttributesFactory);
        }

        @Override
        public AttributeContainer getFrom() {
            return from;
        }

        @Override
        public AttributeContainer getTo() {
            return to;
        }

        @Override
        public void artifactTransform(Class<? extends ArtifactTransform> type) {
            artifactTransform(type, null);
        }

        @Override
        public void artifactTransform(Class<? extends ArtifactTransform> type, Action<? super ActionConfiguration> config) {
            if (this.type != null) {
                throw new VariantTransformConfigurationException("Could not register transform: only one ArtifactTransform may be provided for registration.");
            }
            this.type = type;
            this.config = config;
        }
    }
}