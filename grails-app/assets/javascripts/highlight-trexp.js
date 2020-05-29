(function () {
    if (typeof hljs_at_runtime === 'undefined') { return; }

    // see http://highlightjs.readthedocs.io/en/latest/language-guide.html
    hljs_at_runtime.registerLanguage('trexp', function(hljs) {
        var lang = {
            case_insensitive: true,
            keywords: {
                keyword: 'and or not',
                built_in: 'exists contains datetime'
            },
            contains: [
                // string literals
                hljs.QUOTE_STRING_MODE,

                // number literals
                hljs.C_NUMBER_MODE,

                // boolean literals
                {
                    className: 'number',
                    case_insensitive: true,
                    begin: '(true|false)'
                },

                // TE param-name
                {
                    begin: '\\.',
                    contains: [
                        {
                            className: 'variable',
                            begin: '[A-Za-z_]+'
                        }
                    ]
                },

                // TE operators
                {
                    className: 'meta',
                    begin: '(==|!=|>=|<=|>|<)'
                },

                // TE errors
                {
                    className: 'error',
                    begin: '='
                }
            ]
        };
        console.log(lang);
        return lang;
    });


})();
