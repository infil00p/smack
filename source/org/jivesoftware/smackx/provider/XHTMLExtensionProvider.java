/**
 * $RCSfile$
 * $Revision$
 * $Date$
 *
 * Copyright 2003-2007 Jive Software.
 *
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
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

package org.jivesoftware.smackx.provider;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.XHTMLExtension;
import org.xmlpull.v1.XmlPullParser;

/**
 * The XHTMLExtensionProvider parses XHTML packets.
 *
 * @author Gaston Dombiak
 */
public class XHTMLExtensionProvider implements PacketExtensionProvider {

    /**
     * Creates a new XHTMLExtensionProvider.
     * ProviderManager requires that every PacketExtensionProvider has a public, no-argument constructor
     */
    public XHTMLExtensionProvider() {
    }

    /**
     * Parses a XHTMLExtension packet (extension sub-packet).
     *
     * @param parser the XML parser, positioned at the starting element of the extension.
     * @return a PacketExtension.
     * @throws Exception if a parsing error occurs.
     */
    public PacketExtension parseExtension(XmlPullParser parser)
        throws Exception {
        XHTMLExtension xhtmlExtension = new XHTMLExtension();
        boolean done = false;
        StringBuilder buffer = new StringBuilder();
        int startDepth = parser.getDepth();
        int depth = parser.getDepth();
        String lastTag = "";
        // TODO: Find better way to parse HTML, this code needs to DIAF
        while (!done) {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("body")) {
                    buffer = new StringBuilder();
                    buffer.append("<body xmlns='" +  parser.getNamespace() +  "' >");
                    depth = parser.getDepth();
                }
                else if(parser.getName().equals("hr") || parser.getName().equals("br"))
                {
                	buffer.append("<" + parser.getName() + "/>");
                }
                else
                {
                	buffer.append("<" + parser.getName());
                	for(int i = 0; i < parser.getAttributeCount(); ++i)
                	{
                		buffer.append(" " + parser.getAttributeName(i) + "='" + parser.getAttributeValue(i) + "'");
                	}
                	if (parser.getName().equals("img"))
                		buffer.append(" /");
                	buffer.append(">");
                }
                lastTag = parser.getText();
                if(lastTag != null)
                	buffer.append(lastTag);
            } else if (eventType == XmlPullParser.TEXT) {
                if (buffer != null) {
                    // We need to return valid XML so any inner text needs to be re-escaped
                	String text = parser.getText();
                	if(text != null)
                		buffer.append(StringUtils.escapeForXML(text));
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("body") && parser.getDepth() <= depth) {                	
                    buffer.append("</body>");
                    xhtmlExtension.addBody(buffer.toString());
                }
                else if (parser.getDepth() <= startDepth) {                	
                    done = true;
                }
                else {
                    // This is where we close our tags and do some last minute things.
                	String name = parser.getName();
                	if (name != null && !name.equals("br") && !name.equals("hr") && !name.equals("img"))
                		buffer.append("</" + name + ">");
                }
            }
        }

        System.out.println(xhtmlExtension.toXML());
        return xhtmlExtension;
    }

}
