(function(){
    'use strict';

    angular.module('cts')
        .directive('resultTable', resultTable);

    function resultTable() {
        return {
            restrict: 'E',
            scope: {
                sourceColumn: '=',
                columns: '=',
                ngModel: '='
            },
            replace: true,
            templateUrl: '/shared/results/resultTableView.html'
        }
    }
})();