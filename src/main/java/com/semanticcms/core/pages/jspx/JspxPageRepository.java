/*
 * semanticcms-core-pages-jspx - SemanticCMS pages produced by JSPX in the local servlet container.
 * Copyright (C) 2017, 2018  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of semanticcms-core-pages-jspx.
 *
 * semanticcms-core-pages-jspx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * semanticcms-core-pages-jspx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with semanticcms-core-pages-jspx.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.semanticcms.core.pages.jspx;

import com.aoindustries.net.Path;
import com.aoindustries.util.Tuple2;
import com.semanticcms.core.pages.local.LocalPageRepository;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

/**
 * Accesses JSPX pages, with pattern *.jspx, in the local {@link ServletContext}.
 * Will not match *.inc.jspx.
 *
 * TODO: Block access to *.jspx in the page's local resources, block *.properties, too.
 */
public class JspxPageRepository extends LocalPageRepository {

	private static final String INSTANCES_SERVLET_CONTEXT_KEY = JspxPageRepository.class.getName() + ".instances";

	/**
	 * Gets the JSPX repository for the given context and path.
	 * Only one {@link JspxPageRepository} is created per unique context and path.
	 *
	 * @param  path  Must be a {@link Path valid path}.
	 *               Any trailing slash "/" will be stripped.
	 */
	public static JspxPageRepository getInstance(ServletContext servletContext, Path path) {
		// Strip trailing '/' to normalize
		{
			String pathStr = path.toString();
			if(!pathStr.equals("/") && pathStr.endsWith("/")) {
				path = path.prefix(pathStr.length() - 1);
			}
		}

		Map<Path,JspxPageRepository> instances;
		synchronized(servletContext) {
			@SuppressWarnings("unchecked")
			Map<Path,JspxPageRepository> map = (Map<Path,JspxPageRepository>)servletContext.getAttribute(INSTANCES_SERVLET_CONTEXT_KEY);
			if(map == null) {
				map = new HashMap<Path,JspxPageRepository>();
				servletContext.setAttribute(INSTANCES_SERVLET_CONTEXT_KEY, map);
			}
			instances = map;
		}
		synchronized(instances) {
			JspxPageRepository repository = instances.get(path);
			if(repository == null) {
				repository = new JspxPageRepository(servletContext, path);
				instances.put(path, repository);
			}
			return repository;
		}
	}

	private JspxPageRepository(ServletContext servletContext, Path repositoryPath) {
		super(servletContext, repositoryPath);
	}

	@Override
	public String toString() {
		return "jspx:" + prefix;
	}

	@Override
	protected Tuple2<String,RequestDispatcher> getRequestDispatcher(Path path) throws IOException {
		String pathStr = path.toString();
		// Do not match *.inc.jsp
		if(pathStr.endsWith(".inc")) return null;
		String pathAdd = pathStr.endsWith("/") ? "index.jspx" : ".jspx";
		int len =
			prefix.length()
			+ pathStr.length()
			+ pathAdd.length();
		String resourcePath =
			new StringBuilder(len)
			.append(prefix)
			.append(pathStr)
			.append(pathAdd)
			.toString();
		assert resourcePath.length() == len;
		URL resource = cache.getResource(resourcePath);
		if(resource == null) return null;
		RequestDispatcher dispatcher = servletContext.getRequestDispatcher(resourcePath);
		if(dispatcher != null) {
			return new Tuple2<String, RequestDispatcher>(resourcePath, dispatcher);
		} else {
			return null;
		}
	}
}
