% This function takes in a cell array containing HED tags
% associated with a particular study and validates them based on the
% tags and attributes in the HED XML file.
%
% Usage:
%
%   >>  [errors, warnings, extensions] = validateCellTags(cells);
%
%   >>  [errors, warnings, extensions] = validateCellTags(cells, varargin);
%
% Input:
%
%       cells
%                   A cellstr containing HED tags that are validated.
%
%
%       Optional:
%
%       'extensionAllowed'
%                   True(default) if descendants of extension allowed tags
%                   are accepted which will generate warnings, False if
%                   they are not accepted which will generate errors.
%
%       'hedXML'
%                   The name or the path of the XML file containing
%                   all of the HED tags and their attributes.
%
%       'outputDirectory'
%                   A directory where the validation output is written to
%                   if the 'writeOuput' argument is true.
%                   There will be four separate files generated, one
%                   containing the validation errors, one containing the
%                   validation  warnings, one containing the extension
%                   allowed validation warnings, and a remap file. The
%                   default directory will be the directory that contains
%                   the tab-delimited 'tsvFile'.
%
%       'writeOutput'
%                  True if the validation output is written to the
%                  workspace and a set of files in the same directory,
%                  false (default) if the validation output is only written
%                  to the workspace.
%
% Output:
%
%       errors
%                   A cell array containing all of the validation errors.
%                   Each cell is associated with the validation errors on a
%                   particular line.
%
%       warnings
%                   A cell array containing all of the validation warnings.
%                   Each cell is associated with the validation warnings on
%                   a particular line.
%
%       extensions
%                   A cell array containing all of the extension allowed
%                   validation warnings. Each cell is associated with the
%                   extension allowed validation warnings on a particular
%                   line.
%
% Examples:
%                   To validate the HED study tags in cellstr 'hedTags'.
%
%                   validateCellTags(hedTags);
%
% Copyright (C) 2015 Jeremy Cockfield jeremy.cockfield@gmail.com and
% Kay Robbins, UTSA, kay.robbins@utsa.edu
%
% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License
% along with this program; if not, write to the Free Software
% Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

function [errors, warnings, extensions] = validateCellTags(cells, varargin)
p = parseArguments();
errors = '';
warnings = '';
extensions = '';
hedMaps = loadHEDMap();
mapVersion = hedMaps.version;
xmlVersion = getXMLHEDVersion(p.hedXML);
if ~strcmp(mapVersion, xmlVersion);
    hedMaps = mapHEDAttributes(p.hedXML);
end
if ~all(cellfun(@isempty, strtrim(cells)))
    [errors, warnings, extensions] = parseCellTags(hedMaps, p.cells, ...
        p.extensionAllowed);
    if p.writeOutput
        writeOutputFiles();
    end
end

    function hedMaps = loadHEDMap()
        % Loads a structure that contains Maps associated with the HED XML
        % tags
        Maps = load('HEDMaps.mat');
        hedMaps = Maps.hedMaps;
    end % loadHEDMap

    function p = parseArguments()
        % Parses the arguements passed in and returns the results
        p = inputParser();
        p.addRequired('cells', @iscell);
        p.addParamValue('extensionAllowed', true, ...
            @(x) validateattributes(x, {'logical'}, {})); %#ok<NVREPL>
        p.addParamValue('hedXML', 'HED.xml', ...
            @(x) (~isempty(x) && ischar(x))); %#ok<NVREPL>
        p.addParamValue('outputDirectory', pwd, ...
            @(x) ischar(x) && 7 == exist(x, 'dir')); %#ok<NVREPL>
        p.addParamValue('writeOutput', false, @islogical); %#ok<NVREPL>
        p.parse(cells, varargin{:});
        p = p.Results;
    end % parseArguments

    function writeErrorFile(dir, file, ext)
        % Writes the errors to a file
        numErrors = length(errors);
        errorFile = fullfile(dir, [file '_err' ext]);
        fileId = fopen(errorFile,'w');
        for a = 1:numErrors
            fprintf(fileId, '%s\n', errors{a});
        end
        fclose(fileId);
    end % writeErrorFile

    function writeExtensionFile(dir, file, ext)
        % Writes the extensions to a file
        numExtensions = length(extensions);
        extensionFile = fullfile(dir, [file '_ext' ext]);
        fileId = fopen(extensionFile,'w');
        for a = 1:numExtensions
            fprintf(fileId, '%s\n', extensions{a});
        end
        fclose(fileId);
    end % writeExtensionFile

    function writeOutputFiles()
        % Writes the errors, warnings, extension allowed warnings to
        % the output files
        dir = p.outputDirectory;
        [~, file] = fileparts(p.tsvFile);
        ext = '.txt';
        writeErrorFile(dir, file, ext);
        writeWarningFile(dir, file, ext);
        writeExtensionFile(dir, file, ext);
    end % writeOutputFiles

    function writeWarningFile(dir, file, ext)
        % Writes the warnings to a file
        numWarnings = length(warnings);
        warningFile = fullfile(dir, [file '_wrn' ext]);
        fileId = fopen(warningFile,'w');
        for a = 1:numWarnings
            fprintf(fileId, '%s\n', warnings{a});
        end
        fclose(fileId);
    end % writeWarningFile

end % validateCellTags