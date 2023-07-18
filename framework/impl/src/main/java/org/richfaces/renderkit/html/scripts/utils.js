if (!window.Richfaces) {
	window.Richfaces = {};
}

Richfaces.getComputedStyle = function(eltId, propertyName) {
	var elt = $(eltId);
	
	if (elt.currentStyle) {
		return elt.currentStyle[propertyName];
	}

	if (document.defaultView && document.defaultView.getComputedStyle &&
		document.defaultView.getComputedStyle(elt, null)) {
		return document.defaultView.getComputedStyle(elt, null).getPropertyValue(propertyName);
	}

	return "";
};

Richfaces.getComputedStyleSize = function(eltId, propertyName) {
	var value = Richfaces.getComputedStyle(eltId, propertyName);

	if (value) {
		value = value.strip();	
		value = value.replace(/px$/, "");
		
		return parseFloat(value);
	}
	
	return 0;
};

Richfaces.getWindowSize = function() {
    var myWidth = 0, myHeight = 0;
    if( typeof( window.innerWidth ) == 'number' ) {
        myWidth = window.innerWidth;
        myHeight = window.innerHeight;
    } else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
        myWidth = document.documentElement.clientWidth;
        myHeight = document.documentElement.clientHeight;
    } else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
        myWidth = document.body.clientWidth;
        myHeight = document.body.clientHeight;
    }
	return {"width":myWidth,"height":myHeight};
};

Richfaces.removePX = function(str) {
	var pxIndex = str.indexOf("px")
	if ( pxIndex == -1 ) return str;
	return str.substr(0,pxIndex);
};

Richfaces.visitTree = function(root, callback) {
	var node = root;
	if (!node) {
		node = document;
	}
	
	callback.call(this, node);
	
	var child = node.firstChild;
	while (child) {
		Richfaces.visitTree(child, callback);
		child = child.nextSibling;
	}
};

Richfaces.getNSAttribute = function (name, element) {
	if (element.getAttributeNS) {
		var attr = element.getAttributeNS('http://richfaces.ajax4jsf.org/rich', name);
		if (attr) {
			return attr;
		}
	}
	
	var attributes = element.attributes;
	var attrName = "rich:" + name;
	var attr = attributes[attrName];
	if (attr) {
		return attr.nodeValue;
	}

//	for (var i = 0; i < attributes.length; i++) {
//		attr = attributes[i];
//		if (attr && attrName == attr.nodeName) {
//			return attr.nodeValue;
//		}
//	}	

	return null;
};

Richfaces.VARIABLE_NAME_PATTERN = /^\s*[_,A-Z,a-z][\w,_\.]*\s*$/;

Richfaces.getObjectValue = function (str, object) {
	var a=str.split(".");
	var value=object[a[0]];
	var c=1;
	while (value && c<a.length) value = value[a[c++]];
	return (value ? value : "");
}

Richfaces.evalMacro = function(template, object)
{
	var value="";
	// variable evaluation
	if (Richfaces.VARIABLE_NAME_PATTERN.test(template))
	{
		if (template.indexOf('.')==-1) {
			value = object[template];
			if (!value)	value=window[template];
		}
		// object's variable evaluation
		else {
			value = Richfaces.getObjectValue(template, object);
			if (!value) value=Richfaces.getObjectValue(template, window);
		}
		if (value && typeof value=='function') value = value(object);
		if (!value) value=""; 		
	}
	//js string evaluation
	else {
		try {
			if (Richfaces.browser.isObjectEval) {
				value = object.eval(template);
			}
			else with (object) {
					value = eval(template) ;
			}
			
			if (typeof value == 'function') {
				value = value(object);
			}
		} catch (e) { LOG.warn("Exception: "+e.Message + "\n[" + template + "]"); }
	}
	return value;
}
Richfaces.evalSimpleMacro = function(template, object)
{
	var value = object[template];
	if (!value) {value=window[template]; if (!value) value="";}
	return value;
}

Richfaces.getComponent = function(componentType, element)
{
	var attribute="richfacesComponent";
	var type = "richfaces:"+componentType;
   	while (element.parentNode) {
   		if (element[attribute] && element[attribute]==type)
   			return element.component;
		else
			element = element.parentNode;
   	}
}

Richfaces.browser= {
	isIE: (!window.opera && /MSIE/.test(navigator.userAgent)),
	isIE6: (!window.opera && /MSIE\s*[6][\d,\.]+;/.test(navigator.userAgent)),
	isSafari: /Safari/.test(navigator.userAgent),
	isOpera: !!window.opera,
	isObjectEval: (Richfaces.eval!=undefined)
};

Richfaces.eval = function(template, object) {
	var value = '';
	
	try { 
		with (object) {
			value = eval(template) ;
		} 
	} catch (e) { 
		LOG.warn('Exception: ' + e.message + '\n[' + template + ']'); 
	}

	return value;
};

Richfaces.interpolate = function (placeholders, context) {
	
	for(var k in context) {
		var v = context[k];
		var regexp = new RegExp("\\{" + k + "\\}", "g");
		placeholders = placeholders.replace(regexp, v);
	}
	
	return placeholders;
	
};

if (!Richfaces.position) Richfaces.Position={};

Richfaces.Position.setElementPosition = function(element, baseElement, jointPoint, direction, offset)
{
	// parameters:
	// jointPoint: {x:,y:} or ('top-left','top-right','bottom'-left,'bottom-right')
	// direction:  ('top-left','top-right','bottom'-left,'bottom-right', 'auto')
	// offset: {x:,y:}
	
	var elementDim = Richfaces.Position.getOffsetDimensions(element);
	var baseElementDim = Richfaces.Position.getOffsetDimensions(baseElement);
	
	var windowRect = Richfaces.Position.getWindowViewport();
	
	var baseOffset = Position.cumulativeOffset(baseElement);
	
	// jointPoint
	var ox=baseOffset[0];
	var oy=baseOffset[1];
	var re = /^(top|bottom)-(left|right)$/;
	var match;
	
	if (typeof jointPoint=='object') {ox = jointPoint.x; oy = jointPoint.y}
	else if ( jointPoint && (match=jointPoint.toLowerCase().match(re))!=null )
	{
		if (match[2]=='right') ox+=baseElementDim.width;
		if (match[1]=='bottom') oy+=baseElementDim.height;
	} else
	{
		// ??? auto 
	}
	
	// direction
	if (direction && (match=direction.toLowerCase().match(re))!=null )
	{
		var d = direction.toLowerCase().split('-');
		if (match[2]=='left') { ox-=elementDim.width + offset.x; } else ox +=  offset.x; 
		if (match[1]=='top') { oy-=elementDim.height + offset.y; } else oy +=  offset.y
	} else
	{
		// auto
		var theBest = {square:0};
		// jointPoint: bottom-right, direction: bottom-left
		var rect = {right: baseOffset[0] + baseElementDim.width, top: baseOffset[1] + baseElementDim.height};
		rect.left = rect.right - elementDim.width;
		rect.bottom = rect.top + elementDim.height;
		ox = rect.left; oy = rect.top;
		var s = Richfaces.Position.checkCollision(rect, windowRect);
		if (s!=0)
		{
			if (ox>=0 && oy>=0 && theBest.square<s) theBest = {x:ox, y:oy, square:s};
			// jointPoint: top-right, direction: top-left
			rect = {right: baseOffset[0] + baseElementDim.width, bottom: baseOffset[1]};
			rect.left = rect.right - elementDim.width;
			rect.top = rect.bottom - elementDim.height;
			ox = rect.left; oy = rect.top;
			s = Richfaces.Position.checkCollision(rect, windowRect);
			if (s!=0)
			{
				if (ox>=0 && oy>=0 && theBest.square<s) theBest = {x:ox, y:oy, square:s};
				// jointPoint: bottom-left, direction: bottom-right
				rect = {left: baseOffset[0], top: baseOffset[1] + baseElementDim.height};
				rect.right = rect.left + elementDim.width;
				rect.bottom = rect.top + elementDim.height;
				ox = rect.left; oy = rect.top;
				s = Richfaces.Position.checkCollision(rect, windowRect);
				if (s!=0)
				{
					if (ox>=0 && oy>=0 && theBest.square<s) theBest = {x:ox, y:oy, square:s};
					// jointPoint: top-left, direction: top-right
					rect = {left: baseOffset[0], bottom: baseOffset[1]};
					rect.right = rect.left + elementDim.width;
					rect.top = rect.bottom - elementDim.height;
					ox = rect.left; oy = rect.top;
					s = Richfaces.Position.checkCollision(rect, windowRect);
					if (s!=0)
					{
						// the best way selection
						if (ox<0 || oy<0 || theBest.square>s) {ox=theBest.x; oy=theBest.y}
					}
				}
			}
			
		}
	}	
	
	element.style.left = ox + 'px';
	element.style.top = oy + 'px';
};

Richfaces.Position.getOffsetDimensions = function(element) {
	// from prototype 1.5.0 // Pavel Yascenko
    element = $(element);
    var display = $(element).getStyle('display');
    if (display != 'none' && display != null) // Safari bug
      return {width: element.offsetWidth, height: element.offsetHeight};

    // All *Width and *Height properties give 0 on elements with display none,
    // so enable the element temporarily
    var els = element.style;
    var originalVisibility = els.visibility;
    var originalPosition = els.position;
    var originalDisplay = els.display;
    els.visibility = 'hidden';
    els.position = 'absolute';
    els.display = 'block';
    var originalWidth = element.offsetWidth; // was element.clientWidth // Pavel Yascenko
    var originalHeight = element.offsetHeight; // was element.clientHeight // Pavel Yascenko
    els.display = originalDisplay;
    els.position = originalPosition;
    els.visibility = originalVisibility;
    return {width: originalWidth, height: originalHeight};
};
 
Richfaces.Position.checkCollision = function(elementRect, windowRect, windowOffset)
{
	if (elementRect.left >= windowRect.left &&
		elementRect.top >= windowRect.top &&
		elementRect.right <= windowRect.right &&  
		elementRect.bottom <= windowRect.bottom)
		return 0;
	
	var rect = {left:   (elementRect.left>windowRect.left ? elementRect.left : windowRect.left),
				top:    (elementRect.top>windowRect.top ? elementRect.top : windowRect.top),
				right:  (elementRect.right<windowRect.right ? elementRect.right : windowRect.right),
				bottom: (elementRect.bottom<windowRect.bottom ? elementRect.bottom : windowRect.bottom)};
	return (rect.right-rect.left)* (rect.bottom-rect.top);
};


Richfaces.Position.getWindowDimensions = function() {
    var w =  self.innerWidth
                || document.documentElement.clientWidth
                || document.body.clientWidth
                || 0;
    var h =  self.innerHeight
                || document.documentElement.clientHeight
                || document.body.clientHeight
                || 0;
	return {width:w, height: h};
};

Richfaces.Position.getWindowScrollOffset = function() {
    var dx =  window.pageXOffset
                || document.documentElement.scrollLeft
                || document.body.scrollLeft
                || 0;
    var dy =  window.pageYOffset
                || document.documentElement.scrollTop
                || document.body.scrollTop
                || 0;
	return {left:dx, top: dy};
};

Richfaces.Position.getWindowViewport = function() {
	var windowDim = Richfaces.Position.getWindowDimensions();
	var windowOffset = Richfaces.Position.getWindowScrollOffset();
	return {left:windowOffset.left, top:windowOffset.top, right: windowDim.width+windowOffset.left, bottom: windowDim.height+windowOffset.top};
};

Richfaces.firstDescendant = function(node) {
	var n = node.firstChild;
	while (n && n.nodeType != 1) {
		n = n.nextSibling;
	}

	return n;
};

Richfaces.lastDescendant = function(node) {
	var n = node.lastChild;
	while (n && n.nodeType != 1) {
		n = n.previousSibling;
	}

	return n;
};

Richfaces.next = function(node) {
	var n = node;
	do {
		n = n.nextSibling;
	} while (n && n.nodeType != 1);

	return n;
};

Richfaces.previous = function(node) {
	var n = node;
	do {
		n = n.previousSibling;
	} while (n && n.nodeType != 1);

	return n;
};

Richfaces.removeNode = function(node) {
	if (node) {
		var parentNode = node.parentNode;
		if (parentNode) {
			parentNode.removeChild(node);
		}
	}
}
