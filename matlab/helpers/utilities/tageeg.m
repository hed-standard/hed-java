% Allows a user to tag a EEG structure. First all of the tag information 
% and potential fields are extracted from EEG.event, EEG.urevent, and 
% EEG.etc.tags structures. After existing event tags are extracted and
% merged with an optional input fieldMap, the user is presented with a 
% GUI to accept or exclude potential fields from tagging. Then the user is
% presented with the CTagger GUI to edit and tag. Finally, the tags are 
% rewritten to the EEG structure.
%
% Usage:
%
%   >>  [EEG, fMap, canceled] = tageeg(EEG)
%
%   >>  [EEG, fMap, canceled] = tageeg(EEG, 'key1', 'value1', ...)
%
% Input:
%
%      Required:
%
%      EEG              
%                    The EEG dataset structure that will be tagged. The
%                    dataset will need to have a .event field. 
%
%      Optional (key/value):
%
%      'BaseMap'        
%                    A fieldMap object or the name of a file that contains
%                    a fieldMap object to be used to initialize tag
%                    information.
%
%      'EditXml'        
%                    If false (default), the HED XML cannot be modified
%                    using the tagger GUI. If true, then the HED XML can be
%                    modified using the tagger GUI.
%
%      'ExcludeFields'  
%                    A cell array of field names in the .event and .urevent
%                    substructures to ignore during the tagging process.
%                    By default the following subfields of the event
%                    structure are ignored: .latency, .epoch, .urevent,
%                    .hedtags, and .usertags. The user can over-ride these
%                    tags using this name-value parameter.
%
%      'Fields'         
%                    A cell array of field names of the fields to include
%                    in the tagging. If this parameter is non-empty, only
%                    these fields are tagged.
%
%      'PreservePrefix' 
%                    If false (default), tags for the same field value that
%                    share prefixes are combined and only the most specific
%                    is retained (e.g., /a/b/c and /a/b become just
%                    /a/b/c). If true, then all unique tags are retained.
%
%      'PrimaryField'   
%                    The name of the primary field. Only one field can be
%                    the primary field. A primary field requires a label,
%                    category, and a description. The default is the type
%                    field.
%
%      'SaveMapFile'    
%                    A string representing the file name for saving the
%                    final, consolidated fieldMap object that results from
%                    the tagging process.
%
%      'SaveMode'       
%                    The options are 'OneFile' and 'TwoFiles'. 'OneFile'
%                    saves the EEG structure in a .set file. 'TwoFiles'
%                    saves the EEG structure without the data in a .set
%                    file and the transposed data in a binary float .fdt
%                    file. If the 'Precision' input argument is 'Preserve'
%                    then the 'SaveMode' is ignored and the way that the
%                    file is already saved will be retained.
%
%      'SelectFields'   
%                    If true (default), the user is presented with a
%                    GUI that allow users to select which fields to tag.
%
%      'UseGui'         
%                    If true (default), the CTAGGER GUI is displayed after
%                    initialization.
%
% Output:
%
%       EEG              
%                    The EEG dataset structure that has been tagged. The
%                    tags will be written to the .usertags field under
%                    the .event field.
%
%       fMap
%                    A fieldMap object that stores all of the tags. 
%
%       canceled
%                    True if the tagging has been canceled, False if
%                    otherwise.
%
% Copyright (C) Kay Robbins and Thomas Rognon, UTSA, 2011-2013,
% krobbins@cs.utsa.edu
%
% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License
% along with this program; if not, write to the Free Software
% Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

function [EEG, fMap, canceled] = tageeg(EEG, varargin)
% Parse the input arguments
p = parseArguments(EEG, varargin{:});
canceled = false;
% Get the existing tags for the EEG
[fMap, fMapTag] = findtags(p.EEG, 'PreservePrefix', p.PreservePrefix, ...
     'Fields', p.Fields);
fields = {};
excluded = intersect(p.ExcludeFields, fieldnames(EEG.event));
if p.UseGui && p.SelectFields && isempty(p.Fields)
    fprintf('\n---Now select the fields you want to tag---\n');
    [fMapTag, fields, excluded, canceled] = selectmaps(fMapTag, ...
        'ExcludeFields', excluded, 'PrimaryField', p.PrimaryField);
elseif ~isempty(p.PrimaryField)
    fMapTag.setPrimaryMap(p.PrimaryField);
end

% Exclude the appropriate tags from baseTags
if isa(p.BaseMap, 'fieldMap')
    baseTags = p.BaseMap;
else
    baseTags = fieldMap.loadFieldMap(p.BaseMap);
end
if ~isempty(baseTags) && ~isempty(p.Fields)
    excluded = setdiff(baseTags.getFields(), p.Fields);
end;
fMap.merge(baseTags, 'Merge', excluded, p.Fields);
fMapTag.merge(baseTags, 'Update', excluded, p.Fields);

if p.UseGui && ~canceled
    [fMapTag, canceled] = editmaps(fMapTag, 'EditXml', p.EditXml, ...
        'PreservePrefix', p.PreservePrefix, 'Fields', fields);
end

if ~canceled
    % Replace the existing tags, and then add any new codes found
    fMap.merge(fMapTag, 'Replace', excluded, fMapTag.getFields());
    fMap.merge(fMapTag, 'Merge', excluded, fMapTag.getFields());
    % Save the fieldmap
    if ~isempty(p.SaveMapFile) && ~fieldMap.saveFieldMap(p.SaveMapFile, ...
            fMap)
        warning('tageeg:invalidFile', ...
            ['Couldn''t save fieldMap to ' p.SaveMapFile]);
    end    
    % Now finish writing the tags to the EEG structure
    EEG = writetags(EEG, fMap, 'PreservePrefix', p.PreservePrefix);
    return;
end
fprintf('Tagging was canceled\n');

    function p = parseArguments(EEG, varargin)
        % Parses the input arguments and returns the results
        parser = inputParser;
        parser.addRequired('EEG', @(x) (isempty(x) || isstruct(EEG)));
        parser.addParamValue('BaseMap', '', ...
            @(x)(isempty(x) || ischar(x) || isa(x, 'fieldMap')));
        parser.addParamValue('EditXml', false, @islogical);
        parser.addParamValue('ExcludeFields', ...
            {'latency', 'epoch', 'urevent', 'hedtags', 'usertags'}, ...
            @(x) (iscellstr(x)));
        parser.addParamValue('Fields', {}, @(x) (iscellstr(x)));
        parser.addParamValue('PreservePrefix', false, @islogical);
        parser.addParamValue('PrimaryField', 'type', @(x) ...
            (isempty(x) || ischar(x)))
        parser.addParamValue('SaveMapFile', '', @(x)(isempty(x) || ...
            (ischar(x))));
        parser.addParamValue('SelectFields', true, @islogical);
        parser.addParamValue('UseGui', true, @islogical);
        parser.parse(EEG, varargin{:});
        p = parser.Results;
    end % parseArguments

end % tageeg