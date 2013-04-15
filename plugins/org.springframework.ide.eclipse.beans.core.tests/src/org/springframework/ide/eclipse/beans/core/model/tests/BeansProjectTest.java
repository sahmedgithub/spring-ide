/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansJavaConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class BeansProjectTest {
	
	private IProject project;
	private BeansModel model;
	private BeansProject beansProject;
	private IJavaProject javaProject;

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("beans-config-tests", "org.springframework.ide.eclipse.beans.core.tests");
		javaProject = JdtUtils.getJavaProject(project);
		
		model = new BeansModel();
		beansProject = new BeansProject(model, project);
	}
	
	@After
	public void deleteProject() throws Exception {
		project.delete(true, null);
	}

	@Test
	public void testBeansProjectXMLConfig() throws Exception {
		beansProject.addConfig("basic-bean-config.xml", IBeansConfig.Type.MANUAL);
		
		Set<IBeansConfig> configs = beansProject.getConfigs();
		assertEquals(1, configs.size());
		IBeansConfig config = configs.iterator().next();
		assertEquals("basic-bean-config.xml", config.getElementName());
		assertTrue(config instanceof BeansConfig);
	}
	
	@Test
	public void testBeansProjectJavaConfig() throws Exception {
		beansProject.addConfig("java:org.test.spring.SimpleConfigurationClass", IBeansConfig.Type.MANUAL);
		
		Set<IBeansConfig> configs = beansProject.getConfigs();
		assertEquals(1, configs.size());
		IBeansConfig config = configs.iterator().next();
		assertEquals("java:org.test.spring.SimpleConfigurationClass", config.getElementName());
		assertTrue(config instanceof BeansJavaConfig);
		
		IType type = javaProject.findType("org.test.spring.SimpleConfigurationClass");
		assertEquals(type, ((BeansJavaConfig)config).getConfigClass());
	}
	
	@Test
	public void testBeansProjectMixedConfigs() throws Exception {
		Set<String> configs = new HashSet<String>();
		configs.add("basic-bean-config.xml");
		configs.add("java:org.test.spring.SimpleConfigurationClass");
		beansProject.setConfigs(configs);
		
		IBeansConfig xmlConfig = beansProject.getConfig("basic-bean-config.xml");
		IBeansConfig javaConfig = beansProject.getConfig("java:org.test.spring.SimpleConfigurationClass");
		
		assertEquals("basic-bean-config.xml", xmlConfig.getElementName());
		assertEquals("java:org.test.spring.SimpleConfigurationClass", javaConfig.getElementName());

		assertTrue(xmlConfig instanceof BeansConfig);
		assertTrue(javaConfig instanceof BeansJavaConfig);
		
		IType type = javaProject.findType("org.test.spring.SimpleConfigurationClass");
		assertEquals(type, ((BeansJavaConfig)javaConfig).getConfigClass());
	}
	
}