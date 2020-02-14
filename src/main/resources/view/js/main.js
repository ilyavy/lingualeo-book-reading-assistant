/**
 * Load and anylize chosen book -> loading image.
 * @param status - loading is true / false
 */
function analyzeLoading(status) {
    if (status === true) {
        document.getElementById("button_analyze");
    } else {
        
    }
}

/**
 * Load login -> loading image.
 */
function loginLoading() {
    document.getElementById("auth_form").style.display = "none";
    document.getElementById("auth_loading").style.display = "block";
}

/**
 * Show profile information.
 * @param profileInfo - JsonObject.
 */
function showProfileInfo(profileInfo) {
    document.getElementById("auth").style.display = "none";
    document.getElementById("profile_info").style.display = "table";
    document.getElementsByClassName("profile_nickname")[0].innerHTML = profileInfo.nickname;
    document.getElementsByClassName("profile_lvl")[0].innerHTML = profileInfo.xp_level;
    document.getElementById("profile_words_known").innerHTML = profileInfo.words_known;
    document.getElementById("profile_words_cnt").innerHTML = profileInfo.words_cnt;
    document.getElementById("profile_satiety_percent").innerHTML = profileInfo.hungry_pct;
    document.getElementsByClassName("profile_satiety_progress")[0].style.width = profileInfo.hungry_pct;
}

/**
 * Return to the welcome screen.
 */
function showWelcomeScreen() {
	words = document.getElementById("words");
	words.style.display = "none";
	welcome_screen = document.getElementById("welcome_screen");
	welcome_screen.style.display = "block";
}


/**
 * Check/Uncheck all words checkbox.
 */
function allWords() {
    var checkbox = document.getElementById("words_checkall");
    var state = swapClass(checkbox, "words_checkbox_unchecked", "words_checkbox_checked");
    
    var oldState = "words_word_checkbox_unchecked";
    var newState = "words_word_checkbox_checked";
    if (state === "words_checkbox_unchecked") {
    	var temp = newState;
    	newState = oldState;
    	oldState = temp;
    }
    
    var boxes = document.getElementsByClassName("words_word_checkbox");
    for (var i = 0; i < boxes.length; i++) {
    	changeClass(boxes[i], oldState, newState);
    }
}

/**
 * Check / Uncheck one word
 */
function selectWord(checkbox) {
	swapClass(checkbox, "words_word_checkbox_checked", "words_word_checkbox_unchecked");
}

/**
 * Switch subclass of the specified element between
 * two specified class names.
 * @param element - DOM element
 * @param cName1 - first class name
 * @param cName2 - second class name
 * @returns - installed class name
 */
function swapClass(element, cName1, cName2) {
	var classNames = element.className.split(" ");
	var newClassName = classNames[0];
	var iClassName = "";
	
	for (var i = 1; i < classNames.length; i++) {
		if (classNames[i] === cName1) {
			classNames[i] = cName2;
			iClassName = cName2;
		} else if (classNames[i] === cName2) {
			classNames[i] = cName1;
			iClassName = cName1;
		}
		
		newClassName += (" " + classNames[i]);
	}
	
	element.className = newClassName;
	return iClassName;
}


function changeClass(element, oldName, newName) {
	var classNames = element.className.split(" ");
	var newClassName = classNames[0];
	
	for (var i = 1; i < classNames.length; i++) {
		if (classNames[i] === oldName) {
			classNames[i] = newName;
		}
		newClassName += (" " + classNames[i]);
	}
	
	element.className = newClassName;
}

/**
 * Prints the report from the given json array.
 * @param template
 * @param arr
 * @returns
 */
function printEntitiesList(template, arr, curPage, amountOfPages) {
	welcome_screen = document.getElementById("welcome_screen");
	welcome_screen.style.display = "none";
	words = document.getElementById("words");
	words.style.display = "block";
	
	var text = "";
    for(var i = 0; i < arr.length; i++) {
            obj = arr[i];
            if (obj != null) {
            	var row = template;
            	console.log(template);
            	row = row.replace(/%-id-%/g, "wc_" + obj.id);
            	row = row.replace(/%-frequency-%/g, obj.count);
            	row = row.replace(/%-translate-%/g, obj.word);
            	row = row.replace(/%-context-%/g, obj.context);

                text = text + row;
            }
    }
    document.getElementById("words_list").innerHTML = text;
    
    curPager = document.getElementById("words_paginator_pages_page");
    curPager.value = curPage;
    
    pages = document.getElementById("words_paginator_pages_amount");
    pages.innerHTML = amountOfPages;
}


function selectedWords() {
	var boxes = document.getElementsByClassName("words_word_checkbox_checked");
	var words = new Array(boxes.length);
	
	for (var i = 0; i < boxes.length; i++) {
		var boxHolder = boxes[i].parentNode;
		var wordElement = boxHolder.parentNode;
		var word = {
            id: boxes[i].id.replace("wc_", ""),
			word: wordElement.children[2].innerHTML,
			context: wordElement.children[3].innerHTML
		}
		words[i] = word;
		
		//swapClass(boxes[i], "words_word_checkbox_checked", "words_word_checkbox_unchecked");
		//wordElement.style.display = "none";
	}
	console.log(JSON.stringify(words));
	return JSON.stringify(words);
}


