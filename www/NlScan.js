var exec = require('cordova/exec');

var NlScan = function(){};

/**
 * Constants for checking BCR states
 */

NlScan.prototype.STATE_NONE = 0;     // we're doing nothing
NlScan.prototype.STATE_READING = 1;  //reading BCR reader
NlScan.prototype.STATE_ERROR = 3;    // error
NlScan.prototype.STATE_DESTROYED = 4;// BCR reader destroyed
NlScan.prototype.STATE_READY = 5;    // BCR reader ready

/**
 * init BCR
 *
 * @param successCallback function to be called when plugin is init
 * @param errorCallback well never be called
 */
NlScan.prototype.init = function (successCallback, failureCallback) {
    cordova.exec(
        successCallback,
        failureCallback,
        'NlScan', 'init', []);
};

/**
 * trigger BCR
 *
 * @param successCallback function to be called when plugin is init
 * @param errorCallback well never be called
 */
NlScan.prototype.scan = function (successCallback, failureCallback) {
    cordova.exec(
        successCallback,
        failureCallback,
        'NlScan', 'scan', []);
};

/**
 * destroy BCR
 *
 * @param successCallback function to be called when plugin is destroyed
 * @param errorCallback well never be called
 */
NlScan.prototype.destroy = function(successCallback,failureCallback) {
	cordova.exec(successCallback, failureCallback, 'NlScan', 'destroy', []);
};

/**
 * Check BCR current state
 *
 * @param successCallback(object) returns json object containing state, property state (int)
 * @param errorCallback function to be called when problem fetching state.
 *
 */
NlScan.prototype.getState = function(successCallback,failureCallback) {
		 cordova.exec(successCallback, failureCallback, 'NlScan', 'getState', []);
};

/**
 * Read BCR
 *
 * @param successCallback(data) asynchronous function to be called each time reading was successful.
 * 		returns ASCII string with received data
 * @param errorCallback asynchronous function to be called when there was a problem while reading
 */
NlScan.prototype.read = function(successCallback,failureCallback) {
	 cordova.exec(successCallback, failureCallback, 'NlScan', 'read', []);
};

/**
 * set scanSetting to  timed (scanner off after 3 seconds), start by scan button
 *                  read  (scanner off after read), start by scan button
 *                  continuous (scanner never off), start/end by scan button
 *
 * @param successCallback() function to be called when orientation lock succeeded
 * @param errorCallback() function to be called when orientation lock failed
 * @param para String with required scanSetting
 */
NlScan.prototype.scanSetting = function(successCallback, failureCallback, para) {
		 cordova.exec(successCallback, failureCallback, 'NlScan', 'scanSetting', para);
};

var nlScan = new NlScan();
module.exports = nlScan;