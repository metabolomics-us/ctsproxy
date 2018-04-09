(function() {
    'use strict';

    angular
        .module('cts')
        .factory('download', download);

    download.$inject = ['$timeout'];

    function download($timeout) {

        var service = {
            export: exportFn
        };

        return service;

        //////////

        function exportFn(query, queryStrings, results, style, topHit, type) {
            var data = (style === 'list') ?
                processList(query, queryStrings, results, topHit) :
                processTable(query, queryStrings, results, topHit);

            function pad2(n) {
                return (n < 10 ? '0' : '') + n;
            }

            var date = new Date();
            date = date.getFullYear() + pad2(date.getMonth() + 1) + pad2(date.getDate()) + pad2(date.getHours()) + pad2(date.getMinutes()) + pad2(date.getSeconds());

            var hiddenElement = document.createElement('a');

            hiddenElement.href = 'data:'+ 'text/' + type +',' + encodeURI(data);
            hiddenElement.target = '_blank';
            hiddenElement.download = 'cts-' + date + '.' + type;

            document.body.appendChild(hiddenElement);
            hiddenElement.click();
            document.body.removeChild(hiddenElement);
        }

        function processList(query, searchTerms, results, topHit) {
            var header = 'From,To,Term,Result',
                body = '',
                scored = false;

            for (var i = 0; i < searchTerms.length; i++) {
                var searchTerm = searchTerms[i];

                for (var target in results[searchTerm]) {
                    var resultList = results[searchTerm][target];

                    if (target === 'InChIKey') {
                        scored = true;
                    }

                    body += '\n';

                    if (topHit) {
                        body += (target === 'InChIKey') ?
                            query.from + ',' + target + ',"' + searchTerm + '","' + resultList[0].value + '",' + resultList[0].score :
                            query.from + ',' + target + ',"' + searchTerm + '","' + resultList[0].value + '"';
                    } else {
                        body += resultList.map(function(result) {
                            return (target === 'InChIKey') ?
                                query.from + ',' + target + ',"' + searchTerm + '","' + result.value + '",' + result.score :
                                query.from + ',' + target + ',"' + searchTerm + '","' + result.value + '"';
                        }).join('\n');
                    }
                }
            }

            if (scored) {
                header += ',Score';
            }

            return header + body;
        }

        function processTable(query, searchTerms, results, topHit) {

            var targets = (typeof query.to === 'string') ? [query.to] : query.to,
                data = query.from + ',';

            data += targets.map(function(target) {
                return (target === 'InChIKey') ? target + ',Score' : target
            }).join(',') + '\n';

            data += searchTerms.map(function(searchTerm) {
                return '"' + searchTerm +  '",' + targets.map(function(target) {

                    var resultList = [],
                        scoreList = [];

                    results[searchTerm][target].forEach(function(result) {
                        resultList.push(result.value);

                        if (typeof result.score !== 'undefined') {
                            scoreList.push(result.score);
                        }
                    });

                    var text = topHit ?
                        '"' + resultList[0] + '"' :
                        '"' + resultList.join('\n') + '"';

                    if (scoreList.length > 0) {
                        text += topHit ?
                            ',"' + scoreList[0] + '"' :
                            ',"' + scoreList.join('\n') + '"';
                    }

                    return text;

                }).join(',');
            }).join('\n');

            return data;
        }

    }
})();