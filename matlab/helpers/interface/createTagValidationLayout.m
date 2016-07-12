function [errors, warnings, extensions] = createTagValidationLayout(tab)
latestHED = 'HED.xml';
hedXMLPath = which(latestHED);
hedXML = hedXMLPath;
remapFile = '';
tsvFile = '';
outDir = pwd;
tsvTagColumns = 2;
extensionsAllowed = true;
hasHeader = true;
errorLogOnly = true;
hedXMLCtrl = '';
tsvFileCtrl = '';
remapFileCtrl = '';
outputDirectoryCtrl = '';
createPanel(tab);

    function browseHedXMLCallback(src, eventdata, hedCtrl, ...
            myTitle) %#ok<INUSL>
        % Callback for 'Browse' button that sets the 'HED' editbox
        [tFile, tPath] = uigetfile({'*.xml', 'XML files (*.xml)'}, ...
            myTitle);
        if tFile ~= 0
            hedXML = fullfile(tPath, tFile);
            set(hedCtrl, 'String', hedXML);
        end
    end % browseHedXMLCallback

    function browseOutputDirectoryCallback(src, eventdata, ...
            outputTxtCtrl, myTitle) %#ok<INUSL>
        % Callback for 'Browse' button that sets the 'Output' editbox
        startPath = get(outputTxtCtrl, 'String');
        if isempty(startPath) || ~ischar(startPath) || ~isdir(startPath)
            startPath = pwd;
        end
        dName = uigetdir(startPath, myTitle);
        if dName ~=0
            setOutputDirectory(dName);
        end
    end % browseOutputDirectoryCallback

    function browseTSVFileCallback(src, eventdata, tagsCtrl, ...
            myTitle) %#ok<INUSL>
        % Callback for 'Browse' button that sets the 'Tags' editbox
        [tFile, tPath] = uigetfile({'*.tsv', 'Tab-delimited files'; ...
            '*.txt', 'Text files'; '*.*', 'All files'}, myTitle);
        if tFile ~= 0
            tsvFile = fullfile(tPath, tFile);
            set(tagsCtrl, 'String', tsvFile);
        end
    end % browseTSVFileCallback

    function browseReMapFileCallback(src, eventdata, remapCtrl, ...
            myTitle) %#ok<INUSL>
        % Callback for 'Browse' button that sets the 'Tags' editbox
        [tFile, tPath] = uigetfile({'*.tsv', 'Tab-delimited files'; ...
            '*.txt', 'Text files'; '*.*', 'All files'}, myTitle);
        if tFile ~= 0
            remapFile = fullfile(tPath, tFile);
            set(remapCtrl, 'String', remapFile);
        end
    end % browseReMapFileCallback

    function createButtons(panel)
        % Creates the buttons in the panel
        uicontrol('Parent', panel, ...
            'String', 'Browse', ...
            'Style', 'pushbutton', ...
            'TooltipString', ['Press to choose a XML file containing' ...
            ' all of the HED tags.'], ...
            'Units','normalized',...
            'Callback', {@browseHedXMLCallback, ...
            hedXMLCtrl, 'Browse for HED XML file'}, ...
            'Position', [0.775 0.9 0.2 0.1]);
        uicontrol('Parent', panel, ...
            'String', 'Browse', ...
            'Style', 'pushbutton', ...
            'TooltipString', ['Press to choose a tab-delimited input' ...
            ' file containing study or experiment HED tags'], ...
            'Units','normalized',...
            'Callback', {@browseTSVFileCallback, ...
            tsvFileCtrl, 'Browse for tab-delimited file'}, ...
            'Position', [0.775 0.75 0.2 0.1]);
        uicontrol('Parent', panel, ...
            'String', 'Browse', ...
            'Style', 'pushbutton', ...
            'TooltipString', ...
            ['Press to choose a tab-delimited remap file used to' ...
            ' replace old HED tags with new HED tags.'], ...
            'Units','normalized',...
            'Callback', {@browseReMapFileCallback, ...
            remapFileCtrl, ...
            'Browse for map file'}, ...
            'Position', [0.775 0.6 0.2 0.1]);
        uicontrol('Parent', panel, ...
            'String', 'Browse', ...
            'Style', 'pushbutton', ...
            'TooltipString', ...
            ['Press to choose a directory where the validation' ...
            ' output files are written to.'], ...
            'Units','normalized',...
            'Callback', {@browseOutputDirectoryCallback, ...
            outputDirectoryCtrl, ...
            'Browse for ouput directory'}, ...
            'Position', [0.775 0.45 0.2 0.1]);
        uicontrol('Parent', panel, ...
            'String', 'Help', ...
            'Style', 'pushbutton', ...
            'TooltipString', ...
            'Press for additional instructions.', ...
            'Units','normalized',...
            'Callback', {@helpCallback}, ...
            'Position', [0.775 0.3 0.2 0.1]);
        uicontrol('Parent', panel, ...
            'String', 'Validate', ...
            'Style', 'pushbutton', ...
            'TooltipString', ...
            ['Press to validate a tab-delimited input file' ...
            ' containing study or experiment HED tags against a XML' ...
            ' file containing all of the HED tags'], ...
            'Units','normalized',...
            'Callback', {@validateTSVTagsCallback}, ...
            'Position', [0.775 0.025 0.2 0.1]);
    end % createButtons

    function createCheckboxes()
        % Creates the checkboxes in the panel
        panel = uipanel('Parent', tab, ...
            'BackgroundColor', [.94 .94 .94], ...
            'FontSize', 12, ...
            'Position', [0.15 0.02 0.6 0.2], ...
            'Title', 'Additional options');
        uicontrol('Parent', panel, ...
            'Style', 'checkbox', ...
            'HorizontalAlignment', 'Left', ...
            'String', 'Header', ...
            'Value', hasHeader, ...
            'TooltipString', ['Check if the tab-delimited input file' ...
            ' has a header, uncheck if it does not have a header.'], ...
            'Units','normalized',...
            'Callback', {@hasHeaderCallback}, ...
            'Position', [0.04 0.6 0.25 0.4]);
        uicontrol('Parent', panel, ...
            'Style', 'checkbox', ...
            'HorizontalAlignment', 'Left', ...
            'String', 'Extensions Allowed', ...
            'Value', extensionsAllowed, ...
            'TooltipString', ['Check if descendants of extension' ...
            ' allowed tags are accepted which will generate warnings,' ...
            ' uncheck if they are not accepted which will generate' ...
            ' errors.'], ...
            'Units','normalized',...
            'Callback', {@extensionsAllowedCallback}, ...
            'Position', [0.5 0.6 0.5 0.4]);
        uicontrol('Parent', panel, ...
            'Style', 'checkbox', ...
            'HorizontalAlignment', 'Left', ...
            'String', 'Generate additional output files', ...
            'Value', ~errorLogOnly, ...
            'TooltipString', ['Check if the validation output is' ...
            ' written to the workspace and a set of files in' ...
            ' the same directory, uncheck if the validation ouput is' ...
            ' only written to the workspace.'], ...
            'Units','normalized',...
            'Callback', {@errorLogCallback}, ...
            'Position', [0.04 0.1 0.7 0.4]);
    end % createCheckboxes

    function createEditBoxes(panel)
        % Creates the edit boxes in the panel
        hedXMLCtrl = uicontrol('Parent', panel, ...
            'Style', 'edit', ...
            'BackgroundColor', 'w', ...
            'HorizontalAlignment', 'Left', ...
            'String', hedXMLPath, ...
            'TooltipString', ['A XML file containing all of the HED' ...
            ' tags and their attributes used for event annotation.'], ...
            'Units','normalized',...
            'Callback', {@hedCtrlCallback}, ...
            'Position', [0.15 0.9 0.6 0.1]);
        tsvFileCtrl = uicontrol('Parent', panel, ...
            'Style', 'edit', ...
            'BackgroundColor', 'w', ...
            'HorizontalAlignment', 'Left', ...
            'String', '', ...
            'TooltipString', ['A tab-delimited input file containing' ...
            ' HED tags associated with a particular study or' ...
            ' experiment events.'], ...
            'Units','normalized',...
            'Callback', {@tagsCtrlCallback}, ...
            'Position', [0.15 0.75 0.6 0.1]);
        remapFileCtrl = uicontrol('Parent', panel, ...
            'Style', 'edit', ...
            'BackgroundColor', 'w', ...
            'HorizontalAlignment', 'Left', ...
            'String', '', ...
            'TooltipString', ['A tab-delimited remap file containing' ...
            ' tags that generated errors from a previous validation.' ...
            ' Any new tags that generate errors will be appended to' ...
            ' this file in the first column.'], ...
            'Units','normalized',...
            'Callback', {@remapCtrlCallback}, ...
            'Position', [0.15 0.6 0.6 0.1]);
        outputDirectoryCtrl = uicontrol('Parent', panel, ...
            'Style', 'edit', ...
            'BackgroundColor', 'w', ...
            'HorizontalAlignment', 'Left', ...
            'String', pwd, ...
            'TooltipString', ['A directory where the validation output' ...
            ' is written to if the ''Write Output'' checkbox' ...
            ' is checked.'], ...
            'Units','normalized',...
            'Callback', {@outputCtrlCallback}, ...
            'Position', [0.15 0.45 0.6 0.1]);
        uicontrol('Parent', panel, ...
            'Style', 'edit', ...
            'BackgroundColor', 'w', ...
            'HorizontalAlignment', 'Left', ...
            'Tag', 'validateHedXmlEdit', ...
            'String', '2', ...
            'TooltipString', ['The tag columns in a tab-delimited' ...
            ' input file. The columns can be specified as a single' ...
            ' number or a comma separated list of numbers (e.g. 2 or' ...
            ' 2,3,4)'], ...
            'Units','normalized',...
            'Callback', {@tsvTagColumnsCtrlCallback}, ...
            'Position', [0.15 0.3 0.6 0.1]);
    end % createEditBoxes

    function createLabels(panel)
        % Creates the labels in the panel
        uicontrol('parent', panel, ...
            'Style', 'Text', ...
            'Units', 'normalized', ...
            'String', 'HED file', ...
            'HorizontalAlignment', 'Left', ...
            'Position', [0 0.9 0.12 0.08]);
        uicontrol('parent', panel, ...
            'Style', 'Text', ...
            'Units', 'normalized', ...
            'String', 'Input file', ...
            'HorizontalAlignment', 'Left', ...
            'Position', [0 0.75 0.12 0.08]);
        uicontrol('parent', panel, ...
            'Style', 'Text', ...
            'Units', 'normalized', ...
            'String', 'Remap file (optional)', ...
            'HorizontalAlignment', 'Left', ...
            'Position', [0 0.6 0.12 0.08]);
        uicontrol('parent', panel, ...
            'Style', 'Text', ...
            'Units', 'normalized', ...
            'String', 'Output directory', ...
            'HorizontalAlignment', 'Left', ...
            'Position', [0 0.45 0.12 0.1]);
        uicontrol('parent', panel, ...
            'Style', 'Text', ...
            'Units', 'normalized', ...
            'String', 'Tag columns', ...
            'HorizontalAlignment', 'Left', ...
            'Position', [0 0.3 0.12 0.08]);
    end % createLabels

    function createPanel(tab)
        % Creates the 'Validate Tags' tab panel
        panel = uipanel('Parent', tab, ...
            'BorderType', 'none', ...
            'BackgroundColor', [.94 .94 .94], ...
            'FontSize', 12, ...
            'Position', [0 0 1 1]);
        createLabels(panel);
        createEditBoxes(panel);
        createCheckboxes();
        createButtons(panel);
    end % createPanel

    function tsvTagColumnsCtrlCallback(src, eventdata) %#ok<INUSD>
        % Callback for user directly editing the 'Columns' editbox
        tsvTagColumns = str2num(get(src, 'String')); %#ok<ST2NM>
    end % tsvTagColumnsCtrlCallback

    function extensionsAllowedCallback(src, eventdata) %#ok<INUSD>
        % Callback for user directly editing the 'Extension Allowed'
        % checkbox
        extensionsAllowed = get(src, 'Max') == get(src, 'Value');
    end % extensionAllowedCallback

    function hasHeaderCallback(src, eventdata) %#ok<INUSD>
        % Callback for user directly editing the 'Header' checkbox
        hasHeader = get(src, 'Max') == get(src, 'Value');
    end % hasHeaderCallback

    function errorLogCallback(src, eventdata) %#ok<INUSD>
        % Callback for only generate error log checkbox
        errorLogOnly = ~get(src, 'Max') == get(src, 'Value');
    end % errorLogCallback

    function hedCtrlCallback(src, eventdata) %#ok<INUSD>
        % Callback for user directly editing the 'HED' editbox
        hedXML = get(src, 'String');
    end % hedCtrlCallback

    function remapCtrlCallback(src, eventdata) %#ok<INUSD>
        % Callback for user directly editing the 'HED' editbox
        remapFile = get(src, 'String');
    end % remapCtrlCallback

    function outputCtrlCallback(src, eventdata) %#ok<INUSD>
        % Callback for user directly editing the 'Output' editbox
        outDir = get(src, 'String');
    end % outputCtrlCallback

    function setOutputDirectory(dName)
        % Sets the 'Output' edit box based on the 'Tags' editbox
        outDir = dName;
        set(outputDirectoryCtrl, 'String', outDir);
    end % setOutputDirectory

    function tagsCtrlCallback(src, eventdata) %#ok<INUSD>
        % Callback for user directly editing the 'Tags' editbox
        tsvFile = get(src, 'String');
    end % tagsCtrlCallback

    function helpCallback(src, eventdata) %#ok<INUSD>
        % Callback for the 'Validate' button
        helpdlg(sprintf(['***Main Options***\n\nHED file - The latest HED' ...
            ' schema. This will be the HED.xml file found in the hed' ...
            ' directory by default.\n\nInput file - A' ...
            ' tab-delimited file containing HED tags in a single' ...
            ' column or multiple columns.\n\nRemap file -' ...
            ' If a remap file' ...
            ' is provided then any new errors' ...
            ' found will be appended to the end of it and the filename' ...
            ' is retained. Reusing a map file comes in handy when you' ...
            ' have multiple tab-delimited files that have the same' ...
            ' invalid tags or you simply want to consolidate all of' ...
            ' the changes in one file instead of many.' ...
            '\n\nOutput directory - The directory where' ...
            ' the output files will be written to. The default output' ...
            ' directory will be the current directory.\n\nTag ' ...
            ' columns - The tag columns are to' ...
            ' be specified with a single number or a comma separated' ...
            ' list of numbers (e.g. 1 or 1,2,3,4). The default will be' ...
            ' the second column.\n\n***Additional Options***' ...
            ' \n\nHeader - Check ''Header'' if the the tab-delimited' ...
            ' file has a header. If checked the first row will not be' ...
            ' validated, otherwise it will.\n\nExtensions allowed -' ...
            ' By default tags not in the HED are' ...
            ' accepted that start with the prefix of a tag that has' ...
            ' the extension allowed attribute or is a leaf tag. If you' ...
            ' don''t want this behavior uncheck ''Extensions' ...
            ' allowed.''\n\nGenerate additional output files - ' ...
            ' There will be a error log file and a remap file' ...
            ' generated for the tab-delmited file' ...
            ' that is validated. To generate a warning log file and a' ...
            ' extension log file in addition check ''Generate' ...
            ' additional output files''.']), 'Instructions');
    end % helpCallback

    function validateTSVTagsCallback(src, eventdata) %#ok<INUSD>
        % Callback for the 'Validate' button
        if isempty(hedXML)
            errordlg('HED XML file is empty');
        elseif isempty(tsvFile)
            errordlg('Input file is empty');
        elseif isempty(outDir)
            errordlg('Output directory is empty');
        elseif isempty(tsvTagColumns)
            errordlg('Input file tag columns are empty');
        else
            wb = waitbar(.5,'Please wait...');
            try
                [errors, warnings, extensions] = ...
                    validatetsv(tsvFile, tsvTagColumns, 'hedXML', ...
                    hedXML, 'outDir', outDir, ...
                    'hasHeader', hasHeader, 'extensionAllowed', ...
                    extensionsAllowed, 'writeOutput', true, ...
                    'errorLogOnly', errorLogOnly);
                assignin('base', 'errors', errors);
                assignin('base', 'warnings', warnings);
                assignin('base', 'extensions', extensions);
                msgbox('Complete!');
            catch ME
                errordlg(['Failed!' ME.message]);
            end
            close(wb);
        end
    end % validateTSVTagsCallback

end % createTagValidationLayout