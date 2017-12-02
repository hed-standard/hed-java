import unittest;
from validation.hed_input_reader import HedInputReader;


class Test(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.file_with_extension = 'file_with_extension.txt';
        cls.integer_key_dictionary = {1: 'one', 2: 'two', 3: 'three'};

    def test_get_file_extension(self):
        file_extension = HedInputReader.get_file_extension(self.file_with_extension);
        self.assertIsInstance(file_extension, basestring);
        self.assertTrue(file_extension);

    def test_file_path_has_extension(self):
        file_extension = HedInputReader.file_path_has_extension(self.file_with_extension);
        self.assertIsInstance(file_extension, bool);
        self.assertTrue(file_extension);

    def test_subtract_1_from_dictionary_keys(self):
        one_subtracted_key_dictionary = HedInputReader.subtract_1_from_dictionary_keys(self.integer_key_dictionary);
        self.assertIsInstance(one_subtracted_key_dictionary, dict);
        self.assertTrue(one_subtracted_key_dictionary);
        original_dictionary_key_sum = sum(self.integer_key_dictionary.keys());
        new_dictionary_key_sum = sum(one_subtracted_key_dictionary.keys());
        original_dictionary_key_length = len(self.integer_key_dictionary.keys());
        self.assertEqual(original_dictionary_key_sum - new_dictionary_key_sum, original_dictionary_key_length);



if __name__ == '__main__':
    unittest.main();
