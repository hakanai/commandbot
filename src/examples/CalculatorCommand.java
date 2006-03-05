/*
 * Copyright 2004-2005 Trypticon
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

package examples;

import org.trypticon.xmpp.command.AbstractCommandHandler;

import net.outer_planes.jso.JSO;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.x.commands.CommandQuery;
import org.jabberstudio.jso.x.commands.CommandNote;
import org.jabberstudio.jso.x.disco.DiscoInfoQuery;
import org.jabberstudio.jso.x.xdata.XDataField;
import org.jabberstudio.jso.x.xdata.XDataForm;

/**
 * An exemplary command handler which performs simple calculations for the user.
 */
public class CalculatorCommand extends AbstractCommandHandler
{
    /**
     * Construct the command handler.
     */
    public CalculatorCommand()
    {
        super("http://trypticon.org/commands/examples/calculator", "Calculator");
    }

    /**
     * Handles the command.
     *
     * @param request  the request query.
     * @param response the response query.
     */
    public void handleCommand(CommandQuery request, CommandQuery response)
    {
        if (request.getPayload() == null)
        {
            response.setPayload(buildForm());

            response.setStatus(CommandQuery.EXECUTING);
        }
        else
        {
            XDataForm form = (XDataForm) request.getPayload();
            double param1 = Double.parseDouble(form.getFieldValue("param1"));
            double param2 = Double.parseDouble(form.getFieldValue("param2"));

            double sum = param1 + param2;

            // TODO: Use command notes, when Psi starts supporting them.
//            response.addNote(CommandNote.INFO, "The result is " + sum);
//            response.clearPayload();

            XDataForm resultForm = (XDataForm) form.getDataFactory().createExtensionNode(XDataForm.NAME);
            resultForm.setInstructions("The result is " + sum);
            response.setPayload(resultForm);

            response.setStatus(CommandQuery.COMPLETED);
        }
    }

    /**
     * Builds the form to be displayed to the user.
     *
     * @return the form.
     */
    private XDataForm buildForm()
    {
        StreamDataFactory factory = JSO.getInstance().getDataFactory();
        XDataForm form = (XDataForm) factory.createExtensionNode(XDataForm.NAME);
        form.addInstruction("Enter the two numbers to add.");

        form.addField("param1", XDataField.TEXT_SINGLE);
        form.addField("param2", XDataField.TEXT_SINGLE);

        return form;
    }

    /**
     * Populates info into the provided query, to form the response.
     *
     * @param query the query.
     */
    public void populateDiscoInfo(DiscoInfoQuery query)
    {
        super.populateDiscoInfo(query);
        query.addFeature(XDataForm.NAMESPACE);
    }
}
