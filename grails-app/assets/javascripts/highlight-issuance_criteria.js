(function () {
    if (typeof hljs_at_runtime === 'undefined') { return; }

    // see http://highlightjs.readthedocs.io/en/latest/language-guide.html
    hljs_at_runtime.registerLanguage('issuance_criteria', function(hljs) {
        var lang = {
            case_insensitive: true,
            keywords: {
                keyword: 'and or not',
                built_in: 'yes no na'
            },
            contains: [
                // symbol keywords
                {
                    className: 'keyword',
                    begin: '!'
                },

                // step literals
                {
                    className: 'number',
                    case_insensitive: true,
                    begin: '(all|none)'
                },

                // separators
                {
                    className: 'meta',
                    begin: '(,|\\.\\.\\.)'
                }
            ]
        };
        console.log(lang);
        return lang;
    });


})();
