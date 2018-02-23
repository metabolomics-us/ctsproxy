(function(){
    'use strict';

    angular.module('cts')
        .directive('downloadResults', downloadResults);

    function downloadResults() {
        return {
            restrict: 'E',
            scope: {
                exportStyle: '=',
                exportType: '=',
                topHit: '=',
                downloadFn: '&'
            },
            replace: true,
            templateUrl: '/download/downloadView.html'
        };
    }
})();