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

        function exportFn(query, results, style, topHit, type) {
            var data = (style === 'simplified') ?
                processSimplified(query, results, topHit) :
                process(query, results, topHit);

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

        function processSimplified(query, results, topHit) {
            var data = 'From,To,Term,Result,Score';

            for (var searchTerm in results) {
                for (var target in results[searchTerm]) {
                    var resultList = results[searchTerm][target];

                    data += '\n';

                    if (topHit) {
                        data += (resultList.length > 0) ?
                            (target === 'InChIKey') ?
                                query.from + ',' + target + ',"' + searchTerm + '","' + resultList[0].InChIKey + '",' + resultList[0].score :
                                query.from + ',' + target + ',"' + searchTerm + '","' + resultList[0] + '"' :
                            '';
                    } else {
                        data += resultList.map(function(result) {
                            return (target === 'InChIKey') ?
                                query.from + ',' + target + ',"' + searchTerm + '","' + result.InChIKey + '",' + result.score :
                                query.from + ',' + target + ',"' + searchTerm + '","' + result + '"';
                        }).join('\n');
                    }
                }
            }

            return data;
        }

        function process(query, results, topHit) {

            var source = query.from,
                targets = query.to,
                searchTerms = Object.keys(results),
                data = source + ',';

            data += targets.map(function(target) {
                return (target === 'InChIKey') ? target + ',Score' : target
            }).join(',') + '\n';

            data += searchTerms.map(function(searchTerm) {
                return '"' + searchTerm +  '",' + targets.map(function(target) {

                    var resultList = results[searchTerm][target],
                        inchiList = [],
                        scoreList = [];

                    if (target === 'InChIKey') {
                        resultList.forEach(function(result) {
                            inchiList.push(result.InChIKey);
                            scoreList.push(result.score);
                        });

                        if (topHit) {
                            return (resultList.length > 0) ? '"' + resultList[0].InChIKey + '",' + resultList[0].score : ',';
                        } else {
                            return '"' + inchiList.join('\n') + '","' + scoreList.join('\n') + '"';
                        }
                    } else {
                        if (topHit) {
                            return (resultList.length > 0) ? resultList[0] : '';
                        } else {
                            return '"' + resultList.join('\n') + '"';
                        }
                    }
                }).join(',');
            }).join('\n');

            return data;
        }

    }
})();