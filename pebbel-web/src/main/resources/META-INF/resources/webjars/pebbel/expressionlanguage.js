Ext.define('pebbel.ExpressionLanguageCodeMirrorMode', {
    lexicalContexts: {
        DEFAULT: 0,
        IN_STRING_LITERAL_DOUBLE_QUOTED: 1,
        IN_STRING_LITERAL_SINGLE_QUOTED: 2
    },
    completionTitleTemplate: new Ext.XTemplate(
            '<tpl if="values.descriptorType == \'function\'">',
            '  {[this.renderType(values.returnType)]} {name:htmlEncode}(<tpl for="parameters" between=", ">{[this.renderType(values.type, xindex, xcount, parent.vararg)]} {name:htmlEncode}</tpl>)',
            '</tpl>',
            '<tpl if="values.descriptorType == \'variable\'">',
            '   {name:htmlEncode} {[this.renderType(values.type)]}',
            '</tpl>',
            {
                renderType: function (type, idx, len, vararg) {
                    var split = type.split(".");
                    var unqualifiedType = split[split.length - 1];
                    var encoded = Ext.htmlEncode(unqualifiedType);
                    var notVararg = idx === undefined || idx !== len || !vararg;
                    return notVararg ? encoded.replace(";", "[]") : encoded.replace(";", "&hellip;");
                }
            }
    ),
    constructor: function (options, mode) {
    },
    startState: function () {
        return {
            lc: this.lexicalContexts.DEFAULT
        };
    },
    _renderHints: function (descriptors, token, cursor, cm) {
        var vars = Object.values(descriptors.variables).map(function (d) {
            d.descriptorType = 'variable';
            return d;
        });
        var funs = Object.values(descriptors.functions).map(function (d) {
            d.descriptorType = 'function';
            return d;
        });
        var all = vars.concat(funs);
        var filtered = all.filter(function (v) {
            var tokenImageUpToCurrent = token.string.substr(0, cursor.ch - token.start);
            var notInPrefixCompletion = token.string !== ')' && token.type !== 'variable-name' && token.type !== 'function-name';
            var tokenNameMatchesDescriptor = v.name.indexOf(tokenImageUpToCurrent) === 0 && v.name !== tokenImageUpToCurrent;
            return notInPrefixCompletion || tokenNameMatchesDescriptor;
        });
        if (filtered.length === 0) {
            filtered = all.filter(function (v) {
                //in case nothing matches, we just match by type
                return token.type === (v.descriptorType === 'variable' ? 'variable-name' : 'function-name');
            });
        }
        var sortedByName = filtered.sort(function (lhs, rhs) {
            return lhs.name === rhs.name ? 0 : (lhs.name > rhs.name ? 1 : -1);
        });
        var titleTemplate = this.completionTitleTemplate;
        return sortedByName.map(function (d) {
            var nameIsFollowedByParens = cm.getTokenAt({line: cm.getCursor().line, ch: token.end + 1}).string === '(';
            var shouldAddParenthesis = d.descriptorType === 'function' && !nameIsFollowedByParens;
            return {
                help: [
                    {tag: 'h1', html: titleTemplate.apply(d)},
                    {tag: 'div', html: d.help}
                ],
                text: shouldAddParenthesis ? d.name + "()" : d.name,
                append: token.type !== (d.descriptorType === 'variable' ? 'variable-name' : 'function-name'),
                classes: 'hint-' + d.descriptorType,
                type: d.descriptorType,
                displayText: d.descriptorType === 'function' ? d.name + "()" : d.name,
                offsetCursor: shouldAddParenthesis ? -1 : 0
            };
        });

    },
    hint: function (cm, callback_hints) {
        var self = this;
        var cur = cm.getCursor();
        var precise = true;
        var token = cm.getTokenAt(cur, precise);
        var lc = token.state.lc;
        if (token.start === 0 && token.end === 0) {
            lc = this.lexicalContexts.DEFAULT;
        }
        switch (lc) {
            case this.lexicalContexts.DEFAULT:
            {
                var maybeAutocompletePromise = cm.options.autocompleteRequest(cm.options.panel, cm, cm.getDoc().getValue());
                if (maybeAutocompletePromise === null || maybeAutocompletePromise === undefined) {
                    return;
                }
                var autocompletePromise = maybeAutocompletePromise.then ? maybeAutocompletePromise : Promise.resolve(maybeAutocompletePromise);
                autocompletePromise.then(function (request) {
                    Ext.Ajax.request({
                        url: request.url,
                        jsonData: request.data || {},
                        method: request.method || 'POST',
                        headers: request.headers || {},
                        success: function (r) {
                            var descriptors = JSON.parse(r.responseText);
                            callback_hints(self._renderHints(descriptors, token, cur, cm));
                        },
                        failure: function () {
                            //TODO: failure
                        }
                    });
                });
                break;
            }
            case this.lexicalContexts.IN_STRING_LITERAL_SINGLE_QUOTED:
                callback_hints([
                    {text: "'", append: true, help: ''}
                ]);
                break;
            case this.lexicalContexts.IN_STRING_LITERAL_DOUBLE_QUOTED:
                callback_hints([
                    {text: '"', append: true, help: ''}
                ]);
                break;
            default:
                throw new Error('unknown lexical state');
        }
    },
    token: function (stream, state) {
        if (stream.sol()) {
            state.lc = this.lexicalContexts.DEFAULT;
        }
        switch (state.lc) {
            case this.lexicalContexts.DEFAULT:
                if (stream.match(/^[ \t]+/)) {
                    return 'spaces';
                }
                if (stream.match(/^[A-Z][A-Z0-9_:]*/)) {
                    return 'variable-name';
                }
                if (stream.match(/^[a-z][a-zA-Z0-9_?!:]*/)) {
                    return 'function-name';
                }
                if (stream.match(/^[0-9]+(\.[0-9]*)?/)) {
                    return 'number-literal';
                }
                if (stream.match(/^,/)) {
                    return 'comma';
                }
                if (stream.match(/^&&/)) {
                    return 'boolean-operator-and';
                }
                if (stream.match(/^\|\|/)) {
                    return 'boolean-operator-or';
                }
                if (stream.match(/^[()]/)) {
                    return 'parenthesis';
                }
                if (stream.match(/^'/)) {
                    state.oldlc = state.lc;
                    state.lc = this.lexicalContexts.IN_STRING_LITERAL_SINGLE_QUOTED;
                    return 'string-literal-single-quoted-start';
                }
                if (stream.match(/^"/)) {
                    state.oldlc = state.lc;
                    state.lc = this.lexicalContexts.IN_STRING_LITERAL_DOUBLE_QUOTED;
                    return 'string-literal-double-quoted-start';
                }
                stream.skipToEnd();
                state.lc = this.lexicalContexts.DEFAULT;
                return 'error';
            case this.lexicalContexts.IN_STRING_LITERAL_SINGLE_QUOTED:
                if (stream.match(/^(?:(?:\\')|[^'])+/)) {
                    return 'string-literal-single-quoted';
                }
                if (stream.match(/^'/)) {
                    state.lc = state.oldlc;
                    state.oldlc = undefined;
                    return 'string-literal-single-quoted-end';
                }
                //never happens
            case this.lexicalContexts.IN_STRING_LITERAL_DOUBLE_QUOTED:
                if (stream.match(/^(?:(?:\\")|[^"])+/)) {
                    return 'string-literal-double-quoted';
                }
                if (stream.match(/^"/)) {
                    state.lc = state.oldlc;
                    state.oldlc = undefined;
                    return 'string-literal-double-quoted-end';
                }
                //never happens
            default:
                throw new Error('unknown lexical state');
        }
    }
});

CodeMirror.defineMode("pebbel", pebbel.ExpressionLanguageCodeMirrorMode.create.bind(pebbel.ExpressionLanguageCodeMirrorMode));
CodeMirror.defineMIME("text/x-pebb-el", "pebbel");

Ext.define('pebbel.ExpressionLanguageEditor', {
    extend: 'Ext.form.field.TextArea',
    alias: 'widget.expression-language-editor',
    gutterQickTipTemplate: new Ext.XTemplate('<tpl for="."><div class="single-error">{html}</div></tpl>'),
    constructor: function (c) {
        if (!c.name) {
            throw new Error("name must be configured");
        }
        if (!Ext.isFunction(c.verifyRequest)) {
            throw new Error("verifyRequest must be configured and must be a function");
        }
        if (!Ext.isFunction(c.autocompleteRequest)) {
            throw new Error("autocompleteRequest must be configured and must be a function");
        }

        var defaultConf = {
            errorTemplates: new (c.errorTemplates || pebbel.ErrorTemplates)()
        };
        this.callParent([Ext.merge(defaultConf, c)]);
        this.globalErrorElement = document.createElement("div");
        this.globalErrorElement.className = "cm-global-error";
        this.getActionEl = function () {
            return Ext.get(this.globalErrorElement);
        }.bind(this);

        this.on('resize', function () {
            this.refresh();
        }, this, {delay: 20});
        var self = this;


        var fakeWrapperEl = new Ext.Element(document.createElement('div'));
        var fakeInputEl = new Ext.Element(document.createElement('textarea'));
        fakeWrapperEl.appendChild(fakeInputEl.dom);
        var cm = this._createCm(fakeInputEl.dom, true);
        this.verifyBufferedTask = new Ext.util.DelayedTask(this._highlightParseErrors.bind(this));
        cm.requires = this.requires;
        if (this.initialValue) {
            this.setValue(this.initialValue);
        }
    },
    _createCm: function (textareaEl) {
        var cm = this.codeMirror = CodeMirror.fromTextArea(textareaEl, {
            gutters: ["CodeMirror-linenumbers", 'gutter-errors'],
            extraKeys: {
                "Ctrl-Space": this._autocomplete.bind(this)
            },
            indentUnit: 4,
            lineNumbers: true,
            mode: {name: 'pebbel'},
            verifyRequest: this.verifyRequest,
            autocompleteRequest: this.autocompleteRequest,
            panel: this
        });
        cm.getWrapperElement().appendChild(this.globalErrorElement);
        cm.on('change', function () {
            this.verifyBufferedTask.delay(200);
        }.bind(this));
        return cm;
    },
    refresh: function () {
        this.codeMirror.refresh();
        if (this.bodyEl) {
            this.codeMirror.setSize(this.bodyEl.getSize().width - 2, null);
        }
    },
    onRender: function () {
        this.callSuper(arguments);
        this._createCm(this.inputEl.dom);
    },
    markInvalid: function (msg) {
        var self = this;
        function markInvalidNow() {
            self.getActionEl().show();
            self.setActiveErrors(Ext.Array.from(msg));
        }
        if (this.getEl()) {
            markInvalidNow();
        } else {
            this.on('afterrender', markInvalidNow, this, {single: true});
        }
    },
    clearInvalid: function (msg) {
        this.getActionEl().hide();
        this.callOverridden(msg);
    },
    isValid: function () {
        if (this.forceValidation || !this.disabled) {
            return !!this.verified;
        }
        return !!this.disabled;
    },
    isDirty: function () {
        return false;
    },
    validate: function () {
        return this.isValid();
    },
    getValue: function () {
        if (!this.codeMirror) {
            return this.initialValue;
        }
        return this.codeMirror.getDoc().getValue();
    },
    getModelData: function () {
        var md = {};
        md[this.name] = this.getValue();
        return md;
    },
    getSubmitData: function () {
        return this.getModelData.apply(this, arguments);
    },
    setValue: function (value) {
        this.callSuper(arguments);
        var stringValue = value || "";
        this.latestValue = stringValue;
        this.codeMirror.swapDoc(CodeMirror.Doc(stringValue, {name: "pebbel"}));
        if (stringValue.trim() !== "") {
            this._highlightParseErrors();
        }
    },
    reset: function () {
        this.setValue(this.latestValue || "");
    },
    _autocomplete: function () {
        var cm = this.codeMirror;
        // We want a single cursor position.
        if (cm.somethingSelected()) {
            return;
        }
        if (cm.state.completionActive) {
            cm.state.completionActive.close();
        }

        var hint = cm.getMode().hint.bind(cm.getMode());


        var completion = cm.state.completionActive = new pebbel.Autocompletion(cm, hint, {completeSingle: false, closeOnUnfocus: false});
        CodeMirror.signal(cm, "startCompletion", cm);
        hint(cm, function (hints) {
            completion.showHints(hints);
        }, completion.options);
    },
    _addGutterMessage: function (row, errors) {
        var gm = document.createElement("div");
        gm.innerHTML = "&nbsp;";
        gm.setAttribute('class', 'gutter-error');
        gm.setAttribute('data-qalign', 'tl-tr');
        gm.setAttribute('data-qclass', 'parse-error-tip');
        gm.setAttribute('data-qtitle', 'Error');
        gm.setAttribute('data-qtip', this.gutterQickTipTemplate.apply(errors));
        this.codeMirror.setGutterMarker(row - 1, 'gutter-errors', gm);
    },
    _highlightParseErrors: function () {
        var self = this;
        var cm = self.codeMirror;
        var validBefore = this.isValid();
        var maybeRequestPromise = cm.options.verifyRequest(cm.options.panel, cm, cm.getDoc().getValue());
        if (maybeRequestPromise === null || maybeRequestPromise === undefined) {
            cm.getDoc().getAllMarks().forEach(function (m) {
                m.clear();
            });
            cm.clearGutter('gutter-errors');
            self.verified = self.allowBlank;
            if(!self.allowBlank){
                self.markInvalid(['Expression cannot be empty']);
                self._addGutterMessage(1, {html: 'Expression cannot be empty'});
            }
            return;
        }
        var requestPromise = maybeRequestPromise.then ? maybeRequestPromise : Promise.resolve(maybeRequestPromise);
        requestPromise.then(function (request) {
            Ext.Ajax.request({
                url: request.url,
                method: request.method || 'POST',
                headers: request.headers || {},
                jsonData: request.data,
                success: function (r) {
                    var errors = JSON.parse(r.responseText);
                    self[errors.length === 0 ? 'clearInvalid' : 'markInvalid'](errors);
                    cm.getDoc().getAllMarks().forEach(function (m) {
                        if (m.className === 'cm-parse-error') {
                            m.clear();
                        }
                    });
                    cm.clearGutter('gutter-errors');
                    var formattedErrors = errors.map(self.errorTemplates.render.bind(self.errorTemplates));
                    var errorsByLine = {};
                    formattedErrors.forEach(function (e) {
                        if (!errorsByLine.hasOwnProperty(e.details.source.row)) {
                            errorsByLine[e.details.source.row] = [];
                            errorsByLine[e.details.source.row].push(e);
                        }
                    });
                    for (var line in errorsByLine) {
                        self._addGutterMessage(line, errorsByLine[line]);
                    }
                    formattedErrors.forEach(function (fe) {
                        self.codeMirror.getDoc().markText(
                                {line: fe.details.source.row - 1, ch: fe.details.source.col - 1},
                                {line: fe.details.source.endRow - 1, ch: fe.details.source.endCol},
                                {className: 'cm-parse-error', title: fe.text}
                        );
                    });
                    if (formattedErrors.length !== 0) {
                        self.verified = false;
                        self.markInvalid(['Expression contains errors']);
                    } else {
                        self.verified = true;
                    }
                    var isNowValid = self.isValid();
                    if (validBefore !== isNowValid) {
                        self.fireEvent('validitychange', isNowValid);
                    }
                },
                failure: function (e) {
                    var errors = JSON.parse(e.responseText);
                    cm.getDoc().getAllMarks().forEach(function (m) {
                        m.clear();
                    });
                    cm.clearGutter('gutter-errors');
                    self.verified = false;
                    self.markInvalid(errors[0].reason || 'Server error');
                }
            });
        });
    }
});

Ext.define('pebbel.ErrorTemplates', {
    templates: {
        ARITY_MISMATCH: {
            html: ['Arity mismatch in function {details.symbol:htmlEncode}: expected {details.expected:htmlEncode}, got {details.got:htmlEncode}'],
            text: ['Arity mismatch in function {details.symbol}: expected {details.expected}, got {details.got}']
        },
        TYPE_MISMATCH: {
            html: ['Type mismatch in function {details.symbol:htmlEncode}: expected {details.expected:htmlEncode}, got {details.got:htmlEncode}'],
            text: ['Type mismatch in function {details.symbol}: expected {details.expected}, got {details.got}']
        },
        UNEXPECTED_ERROR_TYPE: {
            text: ['Unknown error: {type:htmlEncode}'],
            html: ['Unknown error: {type}']
        },
        UNKNOWN_SYMBOL: {
            html: ['Unknown symbol: {details.symbol:htmlEncode}'],
            text: ['Unknown symbol: {details.symbol}']
        },
        UNPARSEABLE: {
            html: [
                'Error after: <span class="unnamed">{details.image:htmlEncode}</span>',
                '<h2>Expected one of:</h2>',
                '<tpl for="details.expected"><p><tpl for=".">{[this.tokenToHtml(values)]} </tpl></p></tpl>'
            ],
            text: [
                'Error after: {details.image}\n',
                'Expected one of:\n',
                '<tpl for="details.expected"><tpl for=".">  {[this.tokenToText(values)]}</tpl>\n</tpl>'
            ]
        }
    },
    constructor: function () {
        var self = this;
        this.compiled = {};
        for (var k in this.templates) {
            this.compiled[k] = {
                html: new Ext.XTemplate(this.templates[k].html.concat([self])),
                text: new Ext.XTemplate(this.templates[k].text.concat([self]))
            };
        }
    },
    render: function (error) {
        var template = this.compiled[error.type] || this.compiled.UNEXPECTED_ERROR_TYPE;
        return {
            details: error.details,
            html: template.html.apply(error),
            text: template.text.apply(error)
        };
    },
    tokenToText: function (image) {
        var imageIsQuoted = image.length > 1 && image[0] === '"' && image[image.length - 1] === '"';
        return imageIsQuoted ? image.substr(1, image.length - 2) : image;
    },
    tokenToHtml: function (image) {
        var imageIsQuoted = image.length > 1 && image[0] === '"' && image[image.length - 1] === '"';
        var cleanImage = imageIsQuoted ? image.substr(1, image.length - 2) : image;
        var cls = imageIsQuoted ? "unnamed" : "named";
        return Ext.String.format('<span class="{0}">{1}</span>', cls, Ext.htmlEncode(cleanImage));
    }
});

Ext.define('pebbel.Autocompletion', {
    constructor: function (cm, getHints, options) {
        this.cm = cm;
        this.getHints = getHints;
        this.options = options;
        this.widget = this.onClose = null;
    },
    close: function () {
        if (!this.active()) {
            return;
        }
        this.cm.state.completionActive = null;

        if (this.widget) {
            this.widget.close();
        }
        if (this.onClose) {
            this.onClose();
        }
        CodeMirror.signal(this.cm, "endCompletion", this.cm);
    },
    active: function () {
        return this.cm.state.completionActive === this;
    },
    pick: function (data, i) {
        var completion = data[i];
        var cur = this.cm.getCursor();
        var token = this.cm.getTokenAt(cur, true);
        var line = cur.line;
        if (completion.handler) {
            completion.handler();
        } else {
            this.cm.replaceRange(completion.text, {line: line, ch: completion.append ? token.end : token.start}, {line: line, ch: token.end});
            if (completion.offsetCursor) {
                this.cm.setCursor({line: line, ch: (completion.offsetCursor < 0 ? token.end + completion.text.length : (completion.append ? token.end : token.start)) + completion.offsetCursor});
            }
        }
        this.close();
    },
    showHints: function (data) {
        if (!data || !data.length || !this.active()) {
            return this.close();
        }
        if (this.options.completeSingle !== false && data.length === 1) {
            this.pick(data, 0);
        } else {
            this.showWidget(data);
        }
    },
    showWidget: function (data) {
        this.widget = new pebbel.AutocompletionWidget(this, data);
        CodeMirror.signal(data, "shown");

        var debounce = null;
        var completion = this;
        var finished;
        var closeOn = this.options.closeCharacters || /[\s()\[\]{};:>,]/;
        var startPos = this.cm.getCursor();
        var startLen = this.cm.getLine(startPos.line).length;

        function done() {
            if (finished) {
                return;
            }
            finished = true;
            completion.close();
            completion.cm.off("cursorActivity", activity);
            if (data) {
                CodeMirror.signal(data, "close");
            }
        }

        function update() {
            if (finished) {
                return;
            }
            CodeMirror.signal(data, "update");
            completion.getHints(completion.cm, finishUpdate, completion.options);
        }
        function finishUpdate(data_) {
            data = data_;
            if (finished) {
                return;
            }
            if (!data || !data.length) {
                return done();
            }
            completion.widget = new pebbel.AutocompletionWidget(completion, data);
        }

        function activity() {
            clearTimeout(debounce);
            var pos = completion.cm.getCursor(), line = completion.cm.getLine(pos.line);
            if (pos.line !== startPos.line || line.length - pos.ch !== startLen - startPos.ch || pos.ch < startPos.ch || completion.cm.somethingSelected() || (pos.ch && closeOn.test(line.charAt(pos.ch - 1)))) {
                completion.close();
            } else {
                debounce = setTimeout(update, 170);
                if (completion.widget) {
                    completion.widget.close();
                }
            }
        }
        this.cm.on("cursorActivity", activity);
        this.onClose = done;
    }
});

Ext.define('pebbel.AutocompletionWidget', {
    _createHint: function (cur, i) {
        var c = i !== 0 ? '' : 'active';
        return {
            hintId: i,
            tag: 'li',
            'class': c + ' ' + cur.classes,
            children: [{
                    tag: 'span',
                    'class': 'hint-text',
                    html: cur.html || Ext.htmlEncode(cur.displayText || cur.text)
                }, {
                    tag: 'span',
                    'class': 'hint-type',
                    html: Ext.htmlEncode(cur.type || '')
                }]
        };
    },
    _createHelp: function (cur, i) {
        var c = i !== 0 ? '' : 'active';
        return {
            hintId: i,
            'class': c,
            tag: 'li',
            children: cur.help || [{tag: 'h1', html: cur.text}, {tag: 'div', html: 'No help for this symbol'}]
        };
    },
    constructor: function (completion, data) {
        this.completion = completion;
        this.data = data;
        var widget = this;
        var cm = completion.cm;
        var options = completion.options;

        var hintsContainer = this.hintsContainer = Ext.DomHelper.createDom({
            tag: 'div',
            'class': 'better-hints',
            children: [{
                    tag: 'div',
                    'class': 'cell',
                    children: [{
                            tag: 'ul',
                            'class': 'hints',
                            children: data.map(this._createHint.bind(this))
                        }]
                }, {
                    tag: 'div',
                    'class': 'cell',
                    children: [{
                            tag: 'ul',
                            'class': 'helps' + (data.length && data[0].help.length ? ' active' : ''),
                            children: data.map(this._createHelp.bind(this))
                        }]
                }]
        });
        var hints = this.hints = hintsContainer.children[0].children[0];
        var help = this.help = hintsContainer.children[1].children[0];
        this.selectedHint = 0;

        var pos = cm.cursorCoords(null);
        var left = pos.left, top = pos.bottom, below = true;
        hintsContainer.style.left = left + "px";
        hintsContainer.style.top = top + "px";
        // If we're at the edge of the screen, then we want the menu to appear on the left of the cursor.
        var winW = window.innerWidth || Math.max(document.body.offsetWidth, document.documentElement.offsetWidth);
        var winH = window.innerHeight || Math.max(document.body.offsetHeight, document.documentElement.offsetHeight);
        (options.container || document.body).appendChild(hintsContainer);
        var box = hintsContainer.getBoundingClientRect();
        var overlapX = box.right - winW, overlapY = box.bottom - winH;
        if (overlapX > 0) {
            if (box.right - box.left > winW) {
                hintsContainer.style.width = (winW - 5) + "px";
                overlapX -= (box.right - box.left) - winW;
            }
            hintsContainer.style.left = (left = pos.left - overlapX) + "px";
        }
        if (overlapY > 0) {
            var height = box.bottom - box.top;
            if (box.top - (pos.bottom - pos.top) - height > 0) {
                overlapY = height + (pos.bottom - pos.top);
                below = false;
            } else if (height > winH) {
                hintsContainer.style.height = (winH - 5) + "px";
                overlapY -= height - winH;
            }
            hintsContainer.style.top = (top = pos.bottom - overlapY) + "px";
        }

        cm.addKeyMap(this.keyMap = {
            Up: this.selectPrevious.bind(this),
            Down: this.selectNext.bind(this),
            PageUp: this.selectPreviousPage.bind(this),
            PageDown: this.selectNextPage.bind(this),
            Home: this.selectFirst.bind(this),
            End: this.selectLast.bind(this),
            Enter: this.pick.bind(this),
            Tab: this.pick.bind(this),
            Esc: this.completion.close.bind(this.completion)
        });

        if (options.closeOnUnfocus !== false) {
            var closingOnBlur;
            cm.on("blur", this.onBlur = function () {
                closingOnBlur = setTimeout(function () {
                    completion.close();
                }, 100);
            });
            cm.on("focus", this.onFocus = function () {
                clearTimeout(closingOnBlur);
            });
        }

        var startScroll = cm.getScrollInfo();
        cm.on("scroll", this.onScroll = function () {
            var curScroll = cm.getScrollInfo(), editor = cm.getWrapperElement().getBoundingClientRect();
            var newTop = top + startScroll.top - curScroll.top;
            var point = newTop - (window.pageYOffset || (document.documentElement || document.body).scrollTop);
            if (!below) {
                point += hints.offsetHeight;
            }
            if (point <= editor.top || point >= editor.bottom) {
                return completion.close();
            }
            hints.style.top = newTop + "px";
            hints.style.left = (left + startScroll.left - curScroll.left) + "px";
        });

        function getHintElement(stopAt, el) {
            while (el && el !== stopAt) {
                if (el.nodeName.toUpperCase() === "LI") {
                    return el;
                }
                el = el.parentNode;
            }
        }

        CodeMirror.on(hints, "dblclick", function (e) {
            var t = getHintElement(hints, e.target || e.srcElement);
            if (t && t.getAttribute("hintid") !== null) {
                widget.changeActive(t.getAttribute("hintid"));
                widget.pick();
            }
        });

        CodeMirror.on(hints, "click", function (e) {
            var t = getHintElement(hints, e.target || e.srcElement);
            if (t && t.getAttribute("hintid") !== null) {
                widget.changeActive(t.getAttribute("hintid"));
            }
        });

        CodeMirror.on(hints, "mousedown", function () {
            setTimeout(function () {
                cm.focus();
            }, 20);
        });

        CodeMirror.signal(data, "select", data[0], hints.firstChild);
    },
    close: function () {
        if (this.completion.widget !== this) {
            return;
        }
        this.completion.widget = null;
        this.hintsContainer.parentNode.removeChild(this.hintsContainer);
        this.completion.cm.removeKeyMap(this.keyMap);

        var cm = this.completion.cm;
        if (this.completion.options.closeOnUnfocus !== false) {
            cm.off("blur", this.onBlur);
            cm.off("focus", this.onFocus);
        }
        cm.off("scroll", this.onScroll);
    },
    pick: function () {
        this.completion.pick(this.data, this.selectedHint);
    },
    changeActive: function (i, avoidWrap) {
        if (i >= this.data.length) {
            i = avoidWrap ? this.data.length - 1 : 0;
        } else if (i < 0) {
            i = avoidWrap ? 0 : this.data.length - 1;
        }
        if (this.selectedHint === i) {
            return;
        }
        Ext.fly(this.hints.childNodes[this.selectedHint]).removeCls('active');
        var node = this.hints.childNodes[this.selectedHint = i];
        Ext.fly(node).addCls('active');
        if (node.offsetTop < this.hints.scrollTop) {
            this.hints.scrollTop = node.offsetTop - 3;
        } else if (node.offsetTop + node.offsetHeight > this.hints.scrollTop + this.hints.clientHeight) {
            this.hints.scrollTop = node.offsetTop + node.offsetHeight - this.hints.clientHeight + 3;
        }

        for (var i = 0; i !== this.help.childNodes.length; ++i) {
            Ext.fly(this.help.childNodes[i]).removeCls('active');
        }
        var newActive = this.help.childNodes[this.selectedHint];
        if (newActive.children.length !== 0) {
            Ext.fly(newActive).addCls('active');
            Ext.fly(this.help).addCls('active');
        } else {
            Ext.fly(this.help).removeCls('active');
        }
        CodeMirror.signal(this.data, "select", this.data[this.selectedHint], node);
    },
    screenAmount: function () {
        return Math.floor(this.hints.clientHeight / this.hints.firstChild.offsetHeight) || 1;
    },
    selectPrevious: function () {
        this.changeActive(this.selectedHint - 1, false);
    },
    selectPreviousPage: function () {
        this.changeActive(this.selectedHint - this.screenAmount() + 1, true);
    },
    selectNext: function () {
        this.changeActive(this.selectedHint + 1, false);
    },
    selectNextPage: function () {
        this.changeActive(this.selectedHint + this.screenAmount() - 1, true);
    },
    selectFirst: function () {
        this.changeActive(0);
    },
    selectLast: function () {
        this.changeActive(this.data.length - 1);
    }
});


