(function(){
    'use strict';

    angular.module('cts')
        .run(['$anchorScroll', '$rootScope', function($anchorScroll, $rootScope){
            $anchorScroll.yOffset = 60;

            $rootScope.APP_NAME = 'The Chemical Translation Service';
            $rootScope.APP_NAME_ABBR = 'CTS';
            $rootScope.APP_VERSION = 'v1.0';
        }]);

})();