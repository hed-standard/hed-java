import json;

EMAILS_SENT = 'Email(s) correctly sent';
EMAIL_SUBJECT_KEY = 'Subject';
EMAIL_FROM_KEY = 'From';
EMAIL_TO_KEY = 'To';
EMAIL_BCC_KEY = 'Bcc';
EMAIL_LIST_DELIMITER = ', ';
EMAIL_SENT_RESPONSE = json.dumps({'success': True, 'message': EMAILS_SENT}), 200, {'ContentType': 'application/json'};
GOLLUM = 'gollum';
NO_EMAILS_SENT = 'No email(s) sent. Not in the correct format. Content type needs to be in JSON and X-GitHub-Event'
' must be gollum';
NO_EMAILS_SENT_RESPONSE = json.dumps({'success': True, 'message': NO_EMAILS_SENT}), 200, {
    'ContentType': 'application/json'};
HEADER_EVENT_TYPE = 'X-GitHub-Event';
HEADER_CONTENT_TYPE = 'content-type';
CONTENT_DISPOSITION_HEADER = 'Content-Disposition';
ATTACHMENT_CONTENT_DISPOSITION_HEADER = 'attachment';
JSON_CONTENT_TYPE = 'application/json';
HED_XML_LOCATION_KEY = 'hed_xml_file_location';
HED_WIKI_LOCATION_KEY = 'hed_wiki_file_location';
HED_WIKI_URL = 'https://raw.githubusercontent.com/wiki/BigEEGConsortium/HED-Schema/HED-Schema.mediawiki';
HED_XML_TREE_KEY = 'hed_xml_tree';
HED_XML_VERSION_KEY = 'version';
HED_CHANGE_LOG_KEY = 'hed_change_log';
HED_WIKI_PAGE_KEY = 'HED_WIKI_PAGE'
WIKI_REPOSITORY_KEY = 'repository';
WIKI_REPOSITORY_FULL_NAME_KEY = 'full_name';
WIKI_NOTIFICATIONS_TEXT = 'Wiki notifications';
HED_ATTACHMENT_TEXT = ' Also, the latest HED schema is attached.';
HED_VERSION_TEXT = '\n\nVersion\n';
CHANGE_LOG_TEXT = '\n\nChange log\n';
HED_XML_ATTACHMENT_NAME = 'HED.xml';
WIKI_PAGES_KEY = 'pages';
HELLO_WIKI_TEXT = 'Hello,\nThe wiki page ';
WIKI_TITLE_KEY = 'title';
HAS_BEEN_TEXT = ' has been ';
CHECK_OUT_CHANGES_TEXT = '. Please checkout the changes at ';
WIKI_HTML_URL_KEY = 'html_url';
PERIOD_TEXT = '.';
WIKI_ACTION_KEY = 'action';

def generate_exception_response(ex):
    return json.dumps({'success': False, 'message': ex}), 500, {'ContentType': 'application/json'};
