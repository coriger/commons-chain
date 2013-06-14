/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.chain2.config.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.chain2.Catalog;
import org.apache.commons.chain2.CatalogFactory;
import org.apache.commons.chain2.Context;
import org.apache.commons.chain2.impl.AddingCommand;
import org.apache.commons.chain2.impl.CatalogBase;
import org.apache.commons.chain2.impl.ChainBase;
import org.apache.commons.chain2.impl.ContextBase;
import org.apache.commons.chain2.impl.DelegatingCommand;
import org.apache.commons.chain2.impl.DelegatingFilter;
import org.apache.commons.chain2.impl.ExceptionCommand;
import org.apache.commons.chain2.impl.ExceptionFilter;
import org.apache.commons.chain2.impl.NonDelegatingCommand;
import org.apache.commons.chain2.impl.NonDelegatingFilter;
import org.apache.commons.digester3.Digester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Parameterized test case for {@link XmlConfigParser}, that uses config locations as data points.
 *
 * <p><strong>Note:</strong> This test case assumes, that all config files will be parsed to the same catalog
 * and command instances.</p>
 *
 * @version $Id$
 */
@RunWith(Parameterized.class)
public class XmlConfigParserTestCase {

    private Catalog<String, Object, Context<String, Object>> catalog = null;
    private Context<String, Object> context = null;
    private XmlConfigParser parser = null;
    private final String configLocation;

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][]
            {
                {"/org/apache/commons/chain2/config/xml/test-config.xml"},
                {"/org/apache/commons/chain2/config/xml/test-config-2.xml"}
            }
        );
    }

    public XmlConfigParserTestCase(String configLocation) {
        this.configLocation = configLocation;
    }

    @Before
    public void setUp() throws Exception {
        CatalogFactory.clear();
        catalog = new CatalogBase<String, Object, Context<String, Object>>();
        context = new ContextBase();
        parser = new XmlConfigParser();
    }


    @After
    public void tearDown() {
        parser = null;
        context = null;
        catalog = null;
    }

    // Load the default test-config.xml file and examine the results
    @Test
    public void testDefault() throws Exception {

        // Check overall command count
        load(configLocation);
        checkCommandCount(17);

        // Check individual single command instances
        {
            AddingCommand command = catalog.getCommand("AddingCommand");
            assertNotNull(command);
        }

        {
            DelegatingCommand command = catalog.getCommand("DelegatingCommand");
            assertNotNull(command);
        }

        {
            DelegatingFilter command = catalog.getCommand("DelegatingFilter");
            assertNotNull(command);
        }

        {
            ExceptionCommand command = catalog.getCommand("ExceptionCommand");
            assertNotNull(command);
        }

        {
            ExceptionFilter command = catalog.getCommand("ExceptionFilter");
            assertNotNull(command);
        }

        {
            NonDelegatingCommand command = catalog.getCommand("NonDelegatingCommand");
            assertNotNull(command);
        }

        {
            NonDelegatingFilter command = catalog.getCommand("NonDelegatingFilter");
            assertNotNull(command);
        }

        ChainBase chain = catalog.getCommand("ChainBase");
        assertNotNull(chain);
        assertTrue(chain instanceof TestChain);

        // Check configurable properties instance
        TestCommand tcommand = catalog.getCommand("Configurable");
        assertNotNull(tcommand);
        assertEquals("Foo Value", tcommand.getFoo());
        assertEquals("Bar Value", tcommand.getBar());

    }


    // Test execution of chain "Execute2a"
    @Test
    public void testExecute2a() throws Exception {

        load(configLocation);
        assertTrue("Chain returned true",
                catalog.getCommand("Execute2a").execute(context));
        checkExecuteLog("1/2/3");

    }


    // Test execution of chain "Execute2b"
    @Test
    public void testExecute2b() throws Exception {

        load(configLocation);
        assertTrue("Chain returned false",
                !catalog.getCommand("Execute2b").execute(context));
        checkExecuteLog("1/2/3");

    }


    // Test execution of chain "Execute2c"
    @Test
    public void testExecute2c() throws Exception {

        load(configLocation);
        try {
            catalog.getCommand("Execute2c").execute(context);
        } catch (ArithmeticException e) {
            assertEquals("Correct exception id",
                    "3", e.getMessage());
        }
        checkExecuteLog("1/2/3");

    }


    // Test execution of chain "Execute2d"
    @Test
    public void testExecute2d() throws Exception {

        load(configLocation);
        try {
            catalog.getCommand("Execute2d").execute(context);
        } catch (ArithmeticException e) {
            assertEquals("Correct exception id",
                    "2", e.getMessage());
        }
        checkExecuteLog("1/2");

    }


    // Test execution of chain "Execute4a"
    @Test
    public void testExecute4a() throws Exception {

        load(configLocation);
        assertTrue("Chain returned true",
                catalog.getCommand("Execute4a").execute(context));
        checkExecuteLog("1/2/3/c/a");

    }


    // Test execution of chain "Execute2b"
    @Test
    public void testExecute4b() throws Exception {

        load(configLocation);
        assertTrue("Chain returned false",
                !catalog.getCommand("Execute4b").execute(context));
        checkExecuteLog("1/2/3/b");

    }


    // Test execution of chain "Execute4c"
    @Test
    public void testExecute4c() throws Exception {

        load(configLocation);
        try {
            catalog.getCommand("Execute4c").execute(context);
        } catch (ArithmeticException e) {
            assertEquals("Correct exception id",
                    "3", e.getMessage());
        }
        checkExecuteLog("1/2/3/c/b/a");

    }


    // Test execution of chain "Execute4d"
    @Test
    public void testExecute4d() throws Exception {

        load(configLocation);
        try {
            catalog.getCommand("Execute4d").execute(context);
        } catch (ArithmeticException e) {
            assertEquals("Correct exception id",
                    "2", e.getMessage());
        }
        checkExecuteLog("1/2/b/a");

    }


    // Test a pristine ConfigParser instance
    @Test
    public void testPristine() throws Exception {

        // Validate the "digester" property
        Digester digester = parser.getDigester();
        assertNotNull("Returned a Digester instance", digester);
        assertTrue("Default namespaceAware",
                !digester.getNamespaceAware());
        assertTrue("Default useContextClassLoader",
                digester.getUseContextClassLoader());
        assertTrue("Default validating",
                !digester.getValidating());

        // Validate the "ruleSet" property
        ConfigRuleSet ruleSet = (ConfigRuleSet) parser.getRuleSet();
        assertNotNull("Returned a RuleSet instance", ruleSet);
        assertEquals("Default chainElement",
                "chain", ruleSet.getChainElement());
        assertEquals("Default classAttribute",
                "className", ruleSet.getClassAttribute());
        assertEquals("Default commandElement",
                "command", ruleSet.getCommandElement());
        assertEquals("Default nameAttribute",
                "name", ruleSet.getNameAttribute());
        assertNull("Default namespaceURI",
                ruleSet.getNamespaceURI());

        // Validate the "useContextClassLoader" property
        assertTrue("Defaults to use context class loader",
                parser.getUseContextClassLoader());

        // Ensure that there are no preconfigured commands in the catalog
        checkCommandCount(0);

    }

    // Verify the number of configured commands
    private void checkCommandCount(int expected) {
        int n = 0;
        Iterator<String> names = catalog.getNames();
        while (names.hasNext()) {
            String name = names.next();
            n++;
            assertNotNull(name + " does not exist", catalog.getCommand(name));
        }
        assertEquals("Command count is not correct", expected, n);
    }

    // Verify the contents of the execution log
    private void checkExecuteLog(String expected) {
        StringBuilder log = (StringBuilder) context.get("log");
        assertNotNull("Context did not return log", log);
        assertEquals("Context did not return correct log",
                expected, log.toString());
    }

    // Load the specified catalog from the specified resource path
    private void load(String path) throws Exception {
        URL url = getClass().getResource(path);

        if (url == null) {
            String msg = String.format("Can't find resource for path: %s", path);
            throw new IllegalArgumentException(msg);
        }

        CatalogFactory<String, Object, Context<String, Object>> catalogFactory = parser.parse(url);
        catalog = catalogFactory.getCatalog("foo");
    }

}