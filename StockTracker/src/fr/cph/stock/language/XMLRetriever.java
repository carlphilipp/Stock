/**
 * Copyright 2013 Carl-Philipp Harmant
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

package fr.cph.stock.language;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * This class will read XMLs
 * 
 * @author Carl-Philipp Harmant
 * 
 */
public class XMLRetriever {

	/** The current document xml **/
	private Document document;

	/**
	 * Constructor
	 * 
	 * @param path
	 *            the path of the xml file
	 * @throws IOException
	 * @throws DocumentException
	 */
	public XMLRetriever(String path) throws IOException, DocumentException {
		InputStream inputStream = Resources.getResourceAsStream(path);
		document = parse(inputStream);
	}

	/**
	 * Parser of the input stream
	 * 
	 * @param inputStream
	 *            the stream
	 * @return a Document
	 * @throws DocumentException
	 */
	protected Document parse(InputStream inputStream) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(inputStream);
		return document;
	}

	/**
	 * 
	 * @param node
	 * @return
	 */
	public Node getNode(String node) {
		return document.selectSingleNode(node);
	}

	/**
	 * Get list node
	 * 
	 * @param node
	 *            the node
	 * @return a list of node
	 */
	public List<? extends Node> getListNode(String node) {
		return document.selectNodes(node);
	}
}
