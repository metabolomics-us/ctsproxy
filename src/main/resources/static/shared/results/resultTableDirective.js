(function(){
    'use strict';

    angular.module('cts')
        .directive('resultTable', resultTable);

    function resultTable() {

        function link(scope, element, attrs) {
            scope.page = 1;
            scope.pageSize = 10;

            scope.$watch('ngModel', function(newVal) {
                scope.resultCount = Object.keys(newVal).length;
                scope.pageCount = Math.ceil(scope.resultCount / scope.pageSize);
            }, true);
        }

        return {
            restrict: 'E',
            scope: {
                sourceColumn: '=',
                columns: '=',
                ngModel: '='
            },
            replace: true,
            templateUrl: '/shared/results/resultTableView.html',
            link: link
        }
    }
})();