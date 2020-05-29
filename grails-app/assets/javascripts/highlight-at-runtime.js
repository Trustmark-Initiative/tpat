(function () {
    if (typeof jQuery === 'undefined' || typeof hljs === 'undefined') { return; }
    if (typeof hljs_at_runtime !== 'undefined') { return; }
    var hljsart = window.hljs_at_runtime = {};
    var $ = jQuery;
    var bridge = getBridge();
    var hljsMaxified = bridge.maxify(hljs);

    hljsart.registerLanguage = function registerLanguage(name, languageGetter) {
        // register
        hljs.registerLanguage(name, function languageGetterWrapper(_hljs) {
            var lang = languageGetter(hljsMaxified);
            var minifiedLang = bridge.minify(lang);
            console.log('Added lang at runtime: ', minifiedLang);
            return minifiedLang;
        });

        // highlight any DOM object with correct class on load
        $(function() { $('.' + name).each(function (i, block) { hljs.highlightBlock(block); }); });
    };

    // see https://github.com/isagalaev/highlight.js/blob/master/tools/utility.js
    function getBridge() {
        const REPLACES = {
            'case_insensitive': 'cI',
            'lexemes': 'l',
            'contains': 'c',
            'keywords': 'k',
            'subLanguage': 'sL',
            'className': 'cN',
            'begin': 'b',
            'beginKeywords': 'bK',
            'end': 'e',
            'endsWithParent': 'eW',
            'illegal': 'i',
            'excludeBegin': 'eB',
            'excludeEnd': 'eE',
            'returnBegin': 'rB',
            'returnEnd': 'rE',
            'relevance': 'r',
            'variants': 'v',

            'IDENT_RE': 'IR',
            'UNDERSCORE_IDENT_RE': 'UIR',
            'NUMBER_RE': 'NR',
            'C_NUMBER_RE': 'CNR',
            'BINARY_NUMBER_RE': 'BNR',
            'RE_STARTERS_RE': 'RSR',
            'BACKSLASH_ESCAPE': 'BE',
            'APOS_STRING_MODE': 'ASM',
            'QUOTE_STRING_MODE': 'QSM',
            'PHRASAL_WORDS_MODE': 'PWM',
            'C_LINE_COMMENT_MODE': 'CLCM',
            'C_BLOCK_COMMENT_MODE': 'CBCM',
            'HASH_COMMENT_MODE': 'HCM',
            'NUMBER_MODE': 'NM',
            'C_NUMBER_MODE': 'CNM',
            'BINARY_NUMBER_MODE': 'BNM',
            'CSS_NUMBER_MODE': 'CSSNM',
            'REGEXP_MODE': 'RM',
            'TITLE_MODE': 'TM',
            'UNDERSCORE_TITLE_MODE': 'UTM',
            'COMMENT': 'C',

            'beginRe': 'bR',
            'endRe': 'eR',
            'illegalRe': 'iR',
            'lexemesRe': 'lR',
            'terminators': 't',
            'terminator_end': 'tE'
        };
        var INVERT_REPLACES = {};
        Object.keys(REPLACES).forEach(function (replaceKey) {
            var value = REPLACES[replaceKey];
            INVERT_REPLACES[value] = replaceKey;
        });
        return {
            maxify: function maxify(obj) {
                if (!obj) { return obj; }
                if (Array.isArray(obj)) { return obj.map(maxify); }
                if (typeof obj !== 'object') { return obj; }
                var maxified = {};
                var keys = Object.keys(obj);
                keys.forEach(function (key) {
                    var child = obj[key];
                    var maxifiedChild = maxify(child);
                    var updateKey = INVERT_REPLACES[key] || key;
                    maxified[updateKey] = maxifiedChild;
                });
                return maxified;
            },
            minify: function minify(obj) {
                if (!obj) { return obj; }
                if (Array.isArray(obj)) { return obj.map(minify); }
                if (typeof obj !== 'object') { return obj; }
                var minified = {};
                var keys = Object.keys(obj);
                keys.forEach(function (key) {
                    var child = obj[key];
                    var minifiedChild = minify(child);
                    var updateKey = REPLACES[key] || key;
                    minified[updateKey] = minifiedChild;
                });
                return minified;
            }
        };
    }
})();