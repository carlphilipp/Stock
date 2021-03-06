/**
 * Copyright 2017 Carl-Philipp Harmant
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.stock.language;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * This class will read XMLs
 *
 * @author Carl-Philipp Harmant
 */
public class XMLRetriever {

	/**
	 * The current document xml
	 **/
	private Document document;

	/**
	 * Constructor
	 *
	 * @param path the path of the xml file
	 * @throws DocumentException the document exception
	 */
	public XMLRetriever(final String path) throws DocumentException {
		final URL inputStream = Thread.currentThread().getContextClassLoader().getResource(path);
		document = parse(inputStream);
	}

	/**
	 * Parser of the input stream
	 *
	 * @param inputStream the stream
	 * @return a Document
	 * @throws DocumentException the document exception
	 */

	protected final Document parse(final URL inputStream) throws DocumentException {
		final SAXReader reader = new SAXReader(true);
		reader.setEntityResolver(getEntityResolver());
		return reader.read(inputStream);
	}

	/**
	 * @param node the node
	 * @return a node
	 */
	public final Node getNode(final String node) {
		return document.selectSingleNode(node);
	}

	/**
	 * Get list node
	 *
	 * @param node the node
	 * @return a list of node
	 */
	public final List<? extends Node> getListNode(final String node) {
		return document.selectNodes(node);
	}

	private EntityResolver getEntityResolver() {
		return (publicId, systemId) -> {
			final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("language/Language.dtd");
			return new InputSource(in);
		};
	}
}
