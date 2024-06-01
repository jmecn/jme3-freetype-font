package io.github.jmecn.text;
// line 1 "emoji_presentation_scanner_java.rl"
/* Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class EmojiPresentationScanner {

	static final int emoji_presentation_start = 2;
	private static final byte _emoji_presentation_actions[] = init__emoji_presentation_actions_0();
	private static final byte _emoji_presentation_key_offsets[] = init__emoji_presentation_key_offsets_0();
	private static final byte _emoji_presentation_trans_keys[] = init__emoji_presentation_trans_keys_0();
	private static final byte _emoji_presentation_single_lengths[] = init__emoji_presentation_single_lengths_0();
	private static final byte _emoji_presentation_range_lengths[] = init__emoji_presentation_range_lengths_0();
	private static final byte _emoji_presentation_index_offsets[] = init__emoji_presentation_index_offsets_0();
	private static final byte _emoji_presentation_indicies[] = init__emoji_presentation_indicies_0();
	private static final byte _emoji_presentation_trans_targs[] = init__emoji_presentation_trans_targs_0();
	private static final byte _emoji_presentation_trans_actions[] = init__emoji_presentation_trans_actions_0();
	private static final byte _emoji_presentation_to_state_actions[] = init__emoji_presentation_to_state_actions_0();
	private static final byte _emoji_presentation_from_state_actions[] = init__emoji_presentation_from_state_actions_0();
	private static final byte _emoji_presentation_eof_trans[] = init__emoji_presentation_eof_trans_0();

	// line 20 "EmojiPresentationScanner.java"
	private static byte[] init__emoji_presentation_actions_0() {
		return new byte[]{
				0, 1, 0, 1, 1, 1, 5, 1, 6, 1, 7, 1,
				8, 1, 9, 1, 10, 1, 11, 2, 2, 3, 2, 2,
				4
		};
	}

	private static byte[] init__emoji_presentation_key_offsets_0() {
		return new byte[]{
				0, 5, 7, 14, 18, 20, 21, 24, 29, 30, 34, 36
		};
	}

	private static byte[] init__emoji_presentation_trans_keys_0() {
		return new byte[]{
				3, 7, 13, 0, 2, 14, 15, 2, 3, 6, 7, 13,
				0, 1, 9, 10, 11, 12, 10, 12, 10, 4, 10, 12,
				4, 9, 10, 11, 12, 6, 9, 10, 11, 12, 8, 10,
				9, 10, 11, 12, 14, 0
		};
	}

	private static byte[] init__emoji_presentation_single_lengths_0() {
		return new byte[]{
				3, 2, 5, 4, 2, 1, 3, 5, 1, 4, 2, 5
		};
	}

	private static byte[] init__emoji_presentation_range_lengths_0() {
		return new byte[]{
				1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0
		};
	}

	private static byte[] init__emoji_presentation_index_offsets_0() {
		return new byte[]{
				0, 5, 8, 15, 20, 23, 25, 29, 35, 37, 42, 45
		};
	}

	private static byte[] init__emoji_presentation_indicies_0() {
		return new byte[]{
				2, 1, 1, 1, 0, 4, 5, 3, 8, 9, 10, 11,
				12, 7, 6, 5, 13, 14, 15, 0, 13, 15, 16, 13,
				16, 15, 13, 15, 16, 15, 5, 13, 14, 15, 16, 5,
				17, 5, 13, 14, 18, 17, 5, 13, 16, 5, 13, 14,
				15, 4, 16, 0
		};
	}

	private static byte[] init__emoji_presentation_trans_targs_0() {
		return new byte[]{
				2, 4, 6, 2, 1, 2, 2, 3, 3, 7, 8, 9,
				11, 0, 2, 5, 2, 2, 10
		};
	}

	private static byte[] init__emoji_presentation_trans_actions_0() {
		return new byte[]{
				17, 19, 19, 15, 0, 7, 9, 22, 19, 19, 0, 22,
				19, 0, 5, 19, 11, 13, 19
		};
	}

	private static byte[] init__emoji_presentation_to_state_actions_0() {
		return new byte[]{
				0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0
		};
	}

	private static byte[] init__emoji_presentation_from_state_actions_0() {
		return new byte[]{
				0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0
		};
	}

	private static byte[] init__emoji_presentation_eof_trans_0() {
		return new byte[]{
				1, 4, 0, 1, 17, 17, 17, 17, 18, 18, 17, 17
		};
	}

	public static EmojiIteratorResult scan_emoji_presentation(byte[] data, int p, final int pe) {
		int ts, te;
		final int eof = pe;

		byte act;
		int cs;
		{
			cs = emoji_presentation_start;
			ts = -1;
			te = -1;
			act = 0;
		}

		{
			int _klen;
			int _trans = 0;
			int _acts;
			int _nacts;
			int _keys;
			int _goto_targ = 0;

			while (true) {
				switch (_goto_targ) {
					case 0:
						if (p == pe) {
							_goto_targ = 4;
							continue;
						}
					case 1:
						_acts = _emoji_presentation_from_state_actions[cs];
						_nacts = _emoji_presentation_actions[_acts++];
						while (_nacts-- > 0) {
							switch (_emoji_presentation_actions[_acts++]) {
								case 1: {
									ts = p;
								}
								break;
							}
						}

						_match:
						do {
							_keys = _emoji_presentation_key_offsets[cs];
							_trans = _emoji_presentation_index_offsets[cs];
							_klen = _emoji_presentation_single_lengths[cs];
							if (_klen > 0) {
								int _lower = _keys;
								int _mid;
								int _upper = _keys + _klen - 1;
								while (true) {
									if (_upper < _lower) {
										break;
									}
									_mid = _lower + ((_upper - _lower) >> 1);
									if (data[p] < _emoji_presentation_trans_keys[_mid]) {
										_upper = _mid - 1;
									} else if (data[p] > _emoji_presentation_trans_keys[_mid]) {
										_lower = _mid + 1;
									} else {
										_trans += (_mid - _keys);
										break _match;
									}
								}
								_keys += _klen;
								_trans += _klen;
							}

							_klen = _emoji_presentation_range_lengths[cs];
							if (_klen > 0) {
								int _lower = _keys;
								int _mid;
								int _upper = _keys + (_klen << 1) - 2;
								while (true) {
									if (_upper < _lower) {
										break;
									}
									_mid = _lower + (((_upper - _lower) >> 1) & ~1);
									if (data[p] < _emoji_presentation_trans_keys[_mid]) {
										_upper = _mid - 2;
									} else if (data[p] > _emoji_presentation_trans_keys[_mid + 1]) {
										_lower = _mid + 2;
									} else {
										_trans += ((_mid - _keys) >> 1);
										break _match;
									}
								}
								_trans += _klen;
							}
						}
						while (false);

						_trans = _emoji_presentation_indicies[_trans];
					case 3:
						cs = _emoji_presentation_trans_targs[_trans];

						if (_emoji_presentation_trans_actions[_trans] != 0) {
							_acts = _emoji_presentation_trans_actions[_trans];
							_nacts = _emoji_presentation_actions[_acts++];
							while (_nacts-- > 0) {
								switch (_emoji_presentation_actions[_acts++]) {
									case 2: {
										te = p + 1;
									}
									break;
									case 3: {
										act = 2;
									}
									break;
									case 4: {
										act = 3;
									}
									break;
									case 5: {
										te = p + 1;
										{
											return new EmojiIteratorResult(false, te);
										}
									}
									case 6: {
										te = p + 1;
										{
											return new EmojiIteratorResult(true, te);
										}
									}
									case 7: {
										te = p + 1;
										{
											return new EmojiIteratorResult(false, te);
										}
									}
									case 8: {
										te = p;
										p--;
										{
											return new EmojiIteratorResult(true, te);
										}
									}
									case 9: {
										te = p;
										p--;
										{
											return new EmojiIteratorResult(false, te);
										}
									}
									case 10: {
										{
											p = ((te)) - 1;
										}
										{
											return new EmojiIteratorResult(true, te);
										}
									}
									case 11: {
										switch (act) {
											case 2: {
												{
													p = ((te)) - 1;
												}
												return new EmojiIteratorResult(true, te);
											}
											case 3: {
												{
													p = ((te)) - 1;
												}
												return new EmojiIteratorResult(false, te);
											}
										}
									}
									break;
								}
							}
						}

					case 2:
						_acts = _emoji_presentation_to_state_actions[cs];
						_nacts = _emoji_presentation_actions[_acts++];
						while (_nacts-- > 0) {
							switch (_emoji_presentation_actions[_acts++]) {
								case 0: {
									ts = -1;
								}
								break;
							}
						}

						if (++p != pe) {
							_goto_targ = 1;
							continue;
						}
					case 4:
						if (p == eof) {
							if (_emoji_presentation_eof_trans[cs] > 0) {
								_trans = _emoji_presentation_eof_trans[cs] - 1;
								_goto_targ = 3;
								continue;
							}
						}
					case 5:
				}
				break;
			}
		}

		/* Should not be reached. */
		return new EmojiIteratorResult(false, pe);
	}
}