// make it nice to prototype
jQuery.noConflict();
//memory-leaks sanitizing code
if (!window.RichFaces) {
    window.RichFaces = {};
}

if (!window.RichFaces.Memory) {
    window.RichFaces.Memory = {

        cleaners: {},

        addCleaner: function (name, cleaner) {
            this.cleaners[name] = cleaner;
        },

        applyCleaners: function (node) {
            for (var name in this.cleaners) {
                this.cleaners[name](node);
            }
        },

        clean: function (oldNode) {
            if (oldNode) {
                this.applyCleaners(oldNode);

                //node.all is quicker than recursive traversing
                //window doesn't have "all" attribute
                var all = oldNode.all;

                if (all) {
                    var counter = 0;
                    var length = all.length;

                    for (var counter = 0; counter < length; counter++ ) {
                        this.applyCleaners(all[counter]);
                    }
                } else {
                    var node = oldNode.firstChild;
                    while (node) {
                        this.clean(node);
                        node = node.nextSibling;
                    }
                }
            }
        }
    };

    window.RichFaces.Memory.addCleaner("richfaces", function(node) {
        var component = node.component;
        if (component) {
            var destructorName = component["rich:destructor"];
            //destructor name is required to be back-compatible
            if (destructorName) {
                var destructor = component[destructorName];
                if (destructor) {
                    destructor.call(component);
                }
            }
        }
    });

    if (window.attachEvent) {
        window.attachEvent("onunload", function() {
            var memory = window.RichFaces.Memory;
            memory.clean(document);
            memory.clean(window);
        });
    }
}

//
if (!window.RichFaces) {
    window.RichFaces = {};
}

// calling jQuery(jQuery) makes memory leaks
//if (jQuery(jQuery) != jQuery) {
if (!window.RichFaces.isJQueryWrapped) {
    var oldJQuery = jQuery;
// moved to original jQuery function
//	jQuery = function() {
//		if (arguments[0] == arguments.callee) {
//			return arguments.callee;
//		} else {
//			return oldJQuery.apply(this, arguments);
//		}
//	};

    if (window.RichFaces && window.RichFaces.Memory) {
        window.RichFaces.Memory.addCleaner("jquery", function(node) {
            if (node && node[oldJQuery.expando]) {
                oldJQuery.event.remove(node);
            }
        });
    }
    window.RichFaces.isJQueryWrapped = true;
};
