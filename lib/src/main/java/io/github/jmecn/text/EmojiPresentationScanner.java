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
package io.github.jmecn.text;

public final class EmojiPresentationScanner {

	private EmojiPresentationScanner() {}

	private static final int EMOJI_PRESENTATION_START = 2;
	private static final byte[] EMOJI_PRESENTATION_ACTIONS = {
			0, 1, 0, 1, 1, 1, 5, 1, 6, 1, 7, 1,
			8, 1, 9, 1, 10, 1, 11, 2, 2, 3, 2, 2,
			4
	};
	private static final byte[] EMOJI_PRESENTATION_KEY_OFFSETS = {
			0, 5, 7, 14, 18, 20, 21, 24, 29, 30, 34, 36
	};
	private static final byte[] EMOJI_PRESENTATION_TRANS_KEYS = {
			3, 7, 13, 0, 2, 14, 15, 2, 3, 6, 7, 13,
			0, 1, 9, 10, 11, 12, 10, 12, 10, 4, 10, 12,
			4, 9, 10, 11, 12, 6, 9, 10, 11, 12, 8, 10,
			9, 10, 11, 12, 14, 0
	};
	private static final byte[] EMOJI_PRESENTATION_SINGLE_LENGTHS = {
			3, 2, 5, 4, 2, 1, 3, 5, 1, 4, 2, 5
	};
	private static final byte[] EMOJI_PRESENTATION_RANGE_LENGTHS = {
			1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0
	};
	private static final byte[] EMOJI_PRESENTATION_INDEX_OFFSETS = {
			0, 5, 8, 15, 20, 23, 25, 29, 35, 37, 42, 45
	};
	private static final byte[] EMOJI_PRESENTATION_INDICIES = {
			2, 1, 1, 1, 0, 4, 5, 3, 8, 9, 10, 11,
			12, 7, 6, 5, 13, 14, 15, 0, 13, 15, 16, 13,
			16, 15, 13, 15, 16, 15, 5, 13, 14, 15, 16, 5,
			17, 5, 13, 14, 18, 17, 5, 13, 16, 5, 13, 14,
			15, 4, 16, 0
	};
	private static final byte[] EMOJI_PRESENTATION_TRANS_TARGS = {
			2, 4, 6, 2, 1, 2, 2, 3, 3, 7, 8, 9,
			11, 0, 2, 5, 2, 2, 10
	};
	private static final byte[] EMOJI_PRESENTATION_TRANS_ACTIONS = {
			17, 19, 19, 15, 0, 7, 9, 22, 19, 19, 0, 22,
			19, 0, 5, 19, 11, 13, 19
	};
	private static final byte[] EMOJI_PRESENTATION_TO_STATE_ACTIONS = {
			0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0
	};
	private static final byte[] EMOJI_PRESENTATION_FROM_STATE_ACTIONS = {
			0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0
	};
	private static final byte[] EMOJI_PRESENTATION_EOF_TRANS = {
			1, 4, 0, 1, 17, 17, 17, 17, 18, 18, 17, 17
	};

	public static int scanEmojiPresentation(byte[] data, int p, final int pe) {
		byte act = 0;
		int cs = EMOJI_PRESENTATION_START;
		int te = -1;

		int klen;
		int trans = 0;
		int acts;
		int nacts;
		int keys;
		int gotoTarg = 0;

		while (true) {
			switch (gotoTarg) {
				case 0: {
					if (p == pe) {
						gotoTarg = 4;
						continue;
					}
				}
				case 1: {
					acts = EMOJI_PRESENTATION_FROM_STATE_ACTIONS[cs];
					nacts = EMOJI_PRESENTATION_ACTIONS[acts++];
					while (nacts-- > 0) {
						acts++;
					}

					_match:
					do {
						keys = EMOJI_PRESENTATION_KEY_OFFSETS[cs];
						trans = EMOJI_PRESENTATION_INDEX_OFFSETS[cs];
						klen = EMOJI_PRESENTATION_SINGLE_LENGTHS[cs];
						if (klen > 0) {
							int lower = keys;
							int mid;
							int upper = keys + klen - 1;
							while (upper >= lower) {
								mid = lower + ((upper - lower) >> 1);
								if (data[p] < EMOJI_PRESENTATION_TRANS_KEYS[mid]) {
									upper = mid - 1;
								} else if (data[p] > EMOJI_PRESENTATION_TRANS_KEYS[mid]) {
									lower = mid + 1;
								} else {
									trans += (mid - keys);
									break _match;
								}
							}
							keys += klen;
							trans += klen;
						}

						klen = EMOJI_PRESENTATION_RANGE_LENGTHS[cs];
						if (klen > 0) {
							int lower = keys;
							int mid;
							int upper = keys + (klen << 1) - 2;
							while (upper >= lower) {
								mid = lower + (((upper - lower) >> 1) & ~1);
								if (data[p] < EMOJI_PRESENTATION_TRANS_KEYS[mid]) {
									upper = mid - 2;
								} else if (data[p] > EMOJI_PRESENTATION_TRANS_KEYS[mid + 1]) {
									lower = mid + 2;
								} else {
									trans += ((mid - keys) >> 1);
									break _match;
								}
							}
							trans += klen;
						}
					}
					while (false);

					trans = EMOJI_PRESENTATION_INDICIES[trans];
				}
				case 3: {
					cs = EMOJI_PRESENTATION_TRANS_TARGS[trans];

					if (EMOJI_PRESENTATION_TRANS_ACTIONS[trans] != 0) {
						acts = EMOJI_PRESENTATION_TRANS_ACTIONS[trans];
						nacts = EMOJI_PRESENTATION_ACTIONS[acts++];
						while (nacts-- > 0) {
							switch (EMOJI_PRESENTATION_ACTIONS[acts++]) {
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
									return te;// is_emoji = false
								}
								case 6: {
									te = p + 1;
									return te | 0x80000000;// is_emoji = true
								}
								case 7: {
									te = p + 1;
									return te;// is_emoji = false
								}
								case 8: {
									te = p;
									return te | 0x80000000;// is_emoji = true
								}
								case 9: {
									te = p;
									return te;// is_emoji = false
								}
								case 10: {
									return te | 0x80000000;// is_emoji = true
								}
								case 11: {
									switch (act) {
										case 2: {
											return te | 0x80000000;// is_emoji = true
										}
										case 3: {
											return te;// is_emoji = false
										}
										default:
									}
								}
								break;
							}
						}
					}
				}
				case 2: {
					acts = EMOJI_PRESENTATION_TO_STATE_ACTIONS[cs];
					nacts = EMOJI_PRESENTATION_ACTIONS[acts++];
					while (nacts-- > 0) {
						acts++;
					}

					if (++p != pe) {
						gotoTarg = 1;
						continue;
					}
				}
				case 4: {
					if (EMOJI_PRESENTATION_EOF_TRANS[cs] > 0) {
						trans = EMOJI_PRESENTATION_EOF_TRANS[cs] - 1;
						gotoTarg = 3;
						continue;
					}
					break;
				}
            }
			break;
		}

		/* Should not be reached. */
		return pe; // is_emoji = false
	}
}