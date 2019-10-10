/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.aop.config;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.lang.Nullable;

/**
 * Utility class for handling registration of auto-proxy creators used internally
 * by the '{@code aop}' namespace tags.
 *
 * <p>Only a single auto-proxy creator should be registered and multiple configuration
 * elements may wish to register different concrete implementations. As such this class
 * delegates to {@link AopConfigUtils} which provides a simple escalation protocol.
 * Callers may request a particular auto-proxy creator and know that creator,
 * <i>or a more capable variant thereof</i>, will be registered as a post-processor.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.0
 * @see AopConfigUtils
 */
public abstract class AopNamespaceUtils {

    /**
     * The {@code proxy-target-class} attribute as found on AOP-related XML tags.
     */
    public static final String PROXY_TARGET_CLASS_ATTRIBUTE = "proxy-target-class";

    /**
     * The {@code expose-proxy} attribute as found on AOP-related XML tags.
     */
    private static final String EXPOSE_PROXY_ATTRIBUTE = "expose-proxy";


    public static void registerAutoProxyCreatorIfNecessary(
            ParserContext parserContext, Element sourceElement) {

        BeanDefinition beanDefinition = AopConfigUtils.registerAutoProxyCreatorIfNecessary(
                parserContext.getRegistry(), parserContext.extractSource(sourceElement));
        useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
        registerComponentIfNecessary(beanDefinition, parserContext);
    }

    public static void registerAspectJAutoProxyCreatorIfNecessary(
            ParserContext parserContext, Element sourceElement) {

        BeanDefinition beanDefinition = AopConfigUtils.registerAspectJAutoProxyCreatorIfNecessary(
                parserContext.getRegistry(), parserContext.extractSource(sourceElement));
        useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
        registerComponentIfNecessary(beanDefinition, parserContext);
    }

    public static void registerAspectJAnnotationAutoProxyCreatorIfNecessary(
            ParserContext parserContext, Element sourceElement) {
		/*
		 注册或升级 AutoProxyCreator 定义beanName 为 org.springframework.aop.config.internalAutoProxyCreator
		 */
        BeanDefinition beanDefinition = AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(
                parserContext.getRegistry(), parserContext.extractSource(sourceElement));
		/*
		Spring AOP 部分使用JDK动态代理或者CGLIB来为目标对象创建代理(建议尽量使用JDK的动态代理).如果被代理的目标对象实现了至少一个接口,
		则会使用JDK动态代理.所有该目标类型实现的接口都将被代理.若该目标对象没有实现任何接口,则创建一个CGLIB代理.
		 */
		/*
		对于proxy-target-class 以及 expose-proxy属性的处理
		jdk动态代理:其代理对象必须是某个接口的实现,它是通过在运行期间创建一个接口的实现类来完成对目标对象的代理
		cglib代理:实现原理类似于jdk动态代理,只是它在运行期间生成的代理对象是针对目标类扩展的子类.cglib是高校的代码生成包,底层是依靠ASM(开源
		的Java字节码编辑类库)操作字节码实现的,性能比JDK强
		 */
        useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
		/*
			注册组件并通知,便于监听器做进一步处理,其中beanDefinition的className 为 AnnotationAwareAspectJAutoProxyCreator
		 */
        registerComponentIfNecessary(beanDefinition, parserContext);
    }

    private static void useClassProxyingIfNecessary(BeanDefinitionRegistry registry, @Nullable Element sourceElement) {
        if (sourceElement != null) {
            boolean proxyTargetClass = Boolean.parseBoolean(sourceElement.getAttribute(PROXY_TARGET_CLASS_ATTRIBUTE));
            if (proxyTargetClass) {
                AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
            }
            boolean exposeProxy = Boolean.parseBoolean(sourceElement.getAttribute(EXPOSE_PROXY_ATTRIBUTE));
            if (exposeProxy) {
                AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
            }
        }
    }

    private static void registerComponentIfNecessary(@Nullable BeanDefinition beanDefinition, ParserContext parserContext) {
        if (beanDefinition != null) {
            parserContext.registerComponent(
                    new BeanComponentDefinition(beanDefinition, AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME));
        }
    }

}
