function checkForUpdatesLayout(tab)
currentVersion = findXMLHEDVersion('HED.xml');
createPanel(tab);

    function createButtons(panel)
        % Creates the buttons in the panel
        uicontrol('Parent', panel, ...
            'String', 'Check for updates', ...
            'Style', 'pushbutton', ...
            'TooltipString', 'Press to choose old HED XML file', ...
            'Units','normalized',...
            'Callback', {@updateCallback}, ...
            'Position', [0.775 0 0.2 0.1]);
    end % createButtons

    function createLabels(panel)
        % Creates the labels in the panel
        uicontrol('parent', panel, ...
            'Style', 'Text', ...
            'FontWeight', 'bold', ...
            'Units', 'normalized', ...
            'String', 'Updates', ...
            'HorizontalAlignment', 'Left', ...
            'Position', [0 0.88 0.4 0.12]);
        uicontrol('parent', panel, ...
            'Style', 'Text', ...
            'Units', 'normalized', ...
            'String', ['Checks the HED repository under' ...
            ' BigEEGConsortium for the latest HED XML schema. If the' ...
            ' repository has a newer schema version and you decide' ...
            ' to downloadit then it will replace the local copy.'], ...
            'HorizontalAlignment', 'Left', ...
            'Position', [0 0.84 1 0.12]);
        uicontrol('parent', panel, ...
            'Style', 'Text', ...
            'Units', 'normalized', ...
            'String', ['Current HED version: ' currentVersion], ...
            'HorizontalAlignment', 'Left', ...
            'Position', [0 0 0.5 0.05]);
        
    end % createLabels

    function createPanel(tab)
        % Creates HED mapping layout
        panel = uipanel('Parent', tab, ...
            'BorderType', 'none', ...
            'BackgroundColor', [.94 .94 .94], ...
            'FontSize', 12, ...
            'Position', [0 0 1 1]);
        createLabels(panel);
        createButtons(panel);
    end % createPanel

    function updateCallback(src, evnt) %#ok<INUSD>
        wikiVersion = downloadLatestHED();
        if ~strcmp(currentVersion, wikiVersion)
            okay = createUpdateAvailableLayout(wikiVersion);
            if okay
                updateLatestHED();
                currentVersion = wikiVersion;
                refresh();
                drawnow();
            end
        else
            msgbox('The current version is up to date');
        end
    end % updateCallback

end

