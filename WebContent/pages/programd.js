function focusInput()
{
	var input = document.getElementById('user-input').focus();
}

function submit()
{
	sendToBot(DWRUtil.getValue('user-input'));
}

function sendToBot(message)
{
	if (message != '')
	{
		bot.getResponse(displayResponse, message);
	}
}

var displayResponse = function(response)
{
	DWRUtil.setValue('last-bot-reply', response);
	var history = document.getElementById('dialogue-history');

	var input = DWRUtil.getValue('user-input');
	if (input != '')
	{
		DWRUtil.setValue('last-user-input', input);
		
		var userinput = document.createElement('p');
		userinput.setAttribute('class', 'user-input');
		userinput.innerHTML = input;
		var userinputLabel = document.createElement('span');
		userinputLabel.setAttribute('class', 'label');
		userinputLabel.appendChild(document.createTextNode('you> '));
		userinput.insertBefore(userinputLabel, userinput.childNodes.item(0));

		history.appendChild(userinput);
		
		DWRUtil.setValue('user-input', null);
		document.getElementById('last-user-input-box').style.display = 'block';
	}
	else
	{
		document.getElementById('last-user-input-box').style.display = 'none';
	}
	
	var botreply = document.createElement('p');
	botreply.setAttribute('class', 'bot-reply');
	botreply.innerHTML = response;
	var botreplyLabel = document.createElement('span');
	botreplyLabel.setAttribute('class', 'label');
	botreplyLabel.appendChild(document.createTextNode(botName + '> '));
	botreply.insertBefore(botreplyLabel, botreply.childNodes.item(0));
	
	history.appendChild(botreply);

	focusInput();
}