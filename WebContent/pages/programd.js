/**
 * Puts the cursor into the input field.
 */
function focusInput()
{
	var input = document.getElementById('user-input').focus();
}

/**
 * Sends whatever is in the user-input field to the bot.
 */
function submit()
{
	sendToBot(DWRUtil.getValue('user-input'));
}

/**
 * Sends a non-empty message to the bot, and sets up
 * a callback (thanks to DWR) that will receive the
 * response and display it.
 */
function sendToBot(message)
{
	if (message != '')
	{
		bot.getResponse(displayResponse, message);
	}
}

/**
 * Displays a response sent from the bot, filling/changing
 * the appropriate boxes on the page, and leaves the cursor
 * in the input box.
 */
var displayResponse = function(response)
{
	// Put the response into the last-bot-reply div.
	DWRUtil.setValue('last-bot-reply', response);
	
	// Grab the history box.
	var history = document.getElementById('dialogue-history');

	// Get the input.
	var input = DWRUtil.getValue('user-input');
	
	// Don't bother with empty inputs.
	if (input != '')
	{
		// Put the input into the last-user-input div.
		DWRUtil.setValue('last-user-input', input);
		
		// Create a paragraph that will display the user input in the dialogue history.
		var userinput = document.createElement('p');
		userinput.setAttribute('class', 'user-input');

		// Make a label and append it.
		var userinputLabel = document.createElement('span');
		userinputLabel.setAttribute('class', 'label');
		userinputLabel.appendChild(document.createTextNode('you> '));
		userinput.appendChild(userinputLabel);
		
		// Append the user input text.
		userinput.appendChild(document.createTextNode(input));
		
		// Append this paragraph to the history.
		history.appendChild(userinput);
		
		// Blank out the user input.
		/*
		 * TODO:
		 * Need some way to notify the field that it has changed
		 * (so onchange will work right if the same value is
		 * typed again).
		 */
		DWRUtil.setValue('user-input', '');
	}
	
	// Create a paragraph that will display the reply in the dialogue history.
	var botreply = document.createElement('p');
	botreply.setAttribute('class', 'bot-reply');

	// We set it with (non-standard) .innerHTML so that HTML in the reply will be displayed.  Other ideas?
	botreply.innerHTML = response;
	
	// Make a label.
	var botreplyLabel = document.createElement('span');
	botreplyLabel.setAttribute('class', 'label');
	botreplyLabel.appendChild(document.createTextNode(botName + '> '));
	
	// Insert the label before the already-inserted text/html.
	botreply.insertBefore(botreplyLabel, botreply.childNodes.item(0));
	
	// Append this paragraph.
	history.appendChild(botreply);
	
	// Scroll to the bottom (so the history will be visible).
	window.scrollTo(0, 1000000);

	// Put the cursor back in the input field.
	focusInput();
}