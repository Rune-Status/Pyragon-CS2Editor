package com.cryo.cs2;

import java.util.HashMap;

public enum CS2Instruction {
	PUSH_INT(79, true),
	LOAD_VARP(154, true),
	STORE_VARP(929, true),
	PUSH_STRING(57, true),
	GOTO(123, true),
	INT_NE(693, true),
	INT_EQ(933, true),
	INT_LT(566, true),
	INT_GT(939, true),
	GET_VARP_OLD(267, true),
	GET_VARPBIT_OLD(827, true),
	GET_VARN_OLD(229, true),
	GET_VARNBIT_OLD(526, true),
	RETURN(184, true),
	LOAD_VARPBIT(408, true),
	STORE_VARPBIT(371, true),
	INT_LE(670, true),
	INT_GE(319, true),
	LOAD_INT(500, true),
	STORE_INT(168, true),
	LOAD_STRING(799, true),
	STORE_STRING(356, true),
	MERGE_STRINGS(531, true),
	POP_INT(492, true),
	POP_STRING(354, true),
	CALL_CS2(902, true),
	LOAD_VARC(311, true),
	STORE_VARC(510, true),
	ARRAY_NEW(235, true),
	ARRAY_LOAD(859, true),
	ARRAY_STORE(116, true),
	LOAD_VARC_STRING(700, true),
	STORE_VARC_STRING(21, true),
	SWITCH(377, true),
	PUSH_LONG(633, true),
	POP_LONG(539, true),
	LOAD_LONG(473, true),
	STORE_LONG(987, true),
	LONG_NE(908, true),
	LONG_EQ(381, true),
	LONG_LT(597, true),
	LONG_GT(237, true),
	LONG_LE(465, true),
	LONG_GE(142, true),
	BRANCH_EQ1(195, true),
	BRANCH_EQ0(836, true),
	LOAD_CLAN_VAR(717, true),
	LOAD_CLAN_VARBIT(220, true),
	LOAD_CLAN_VAR_LONG(215, true),
	LOAD_CLAN_VAR_STRING(995, true),
	LOAD_CLAN_SETTING_VAR(73, true),
	LOAD_CLAN_SETTING_VARBIT(147, true),
	LOAD_CLAN_SETTING_VAR_LONG(83, true),
	LOAD_CLAN_SETTING_VAR_STRING(910, true),
	CC_CREATE(113),
	CC_DELETE(981),
	CC_DELETEALL(572),
	CC_FIND(677),
	instr6004(333),
	IF_SENDTOFRONT(515),
	IF_SENDTOBACK(246),
	CC_SENDTOFRONT(993),
	CC_SENDTOBACK(584),
	IF_RESUME_PAUSEBUTTON(326),
	CC_RESUME_PAUSEBUTTON(502),
	BASE_IDKIT(613),
	BASE_COLOR(156),
	SET_GENDER(325),
	SET_ITEM(925),
	CC_SETPOSITION(847),
	CC_SETSIZE(140),
	CC_SETHIDE(112),
	CC_SETASPECT(341),
	CC_SETNOCLICKTHROUGH(787),
	CC_SETSCROLLPOS(126),
	CC_SETCOLOR(274),
	CC_SETFILL(402),
	CC_SETTRANS(320),
	CC_SETLINEWID(124),
	CC_SETGRAPHIC(415),
	CC_SET2DANGLE(196),
	CC_SETTILING(406),
	CC_SETMODEL(872),
	CC_SETMODELANGLE(528),
	CC_SETMODELANIM(32),
	CC_SETMODELORTHOG(363),
	CC_SETMODELTINT(911),
	CC_SETTEXT(972),
	CC_SETTEXTFONT(351),
	CC_SETTEXTALIGN(546),
	CC_SETTEXTSHADOW(103),
	CC_SETTEXTANTIMACRO(644),
	CC_SETOUTLINE(317),
	CC_SETGRAPHICSHADOW(751),
	CC_SETVFLIP(599),
	CC_SETHFLIP(185),
	CC_SETSCROLLSIZE(28),
	CC_SETALPHA(209),
	CC_SETMODELZOOM(159),
	CC_SETLINEDIRECTION(965),
	CC_SETMODELORIGIN(24),
	CC_SETMAXLINES(77),
	CC_SETPARAM_INT(497),
	CC_SETPARAM_STRING(639),
	instr6050(513),
	instr6051(100),
	CC_SETRECOL(589),
	CC_SETRETEX(579),
	CC_SETFONTMONO(476),
	CC_SETPARAM(606),
	CC_SETCLICKMASK(924),
	CC_SETITEM(13),
	CC_SETNPCHEAD(542),
	CC_SETPLAYERHEAD_SELF(186),
	CC_SETNPCMODEL(470),
	CC_SETPLAYERMODEL(133),
	CC_SETITEM_NONUM(412),
	instr6063(591),
	instr6064(314),
	instr6065(878),
	instr6066(173),
	instr6895(928),
	CC_SETPLAYERMODEL_SELF(443),
	CC_SETITEM_ALWAYSNUM(998),
	CC_SETITEM_WEARCOL_ALWAYSNUM(834),
	CC_SETOP(671),
	instr6676(68),
	instr6073(345),
	instr6443(764),
	instr6075(223),
	CC_SETOPBASE(275),
	instr6110(466),
	instr6179(611),
	instr6218(627),
	instr6080(176),
	instr6081(806),
	instr6737(659),
	instr6083(66),
	instr6258(749),
	instr6085(895),
	instr6086(67),
	instr6087(219),
	instr6088(813),
	instr5957(158),
	instr6450(374),
	instr6091(1),
	instr6092(665),
	instr6224(705),
	instr6094(892),
	instr6095(697),
	instr6556(957),
	instr6687(457),
	instr6499(392),
	instr6084(708),
	instr6346(207),
	instr6452(191),
	instr6899(886),
	instr6103(991),
	instr6765(615),
	instr6370(807),
	instr6106(695),
	instr6580(323),
	instr6437(59),
	instr6903(841),
	instr6099(883),
	instr6111(719),
	instr6112(490),
	instr6113(440),
	instr6492(652),
	instr6096(315),
	instr6116(70),
	instr6054(525),
	instr6782(916),
	instr6119(505),
	instr6120(544),
	instr6121(702),
	instr6552(797),
	CC_GETY(523),
	instr6124(545),
	CC_GETHEIGHT(796),
	instr6344(747),
	instr6462(204),
	instr6128(626),
	instr6762(451),
	instr6130(494),
	instr6131(755),
	instr6132(979),
	instr6133(680),
	instr6134(829),
	instr6135(0),
	instr6901(491),
	instr6529(648),
	instr6670(555),
	instr6139(34),
	instr6005(97),
	instr6262(728),
	instr6928(449),
	instr6771(832),
	instr6144(723),
	instr6230(664),
	instr6566(160),
	instr6842(472),
	instr6148(137),
	instr6636(36),
	instr6150(3),
	instr6151(620),
	instr6585(467),
	instr6153(75),
	instr6154(125),
	instr6155(694),
	instr6662(707),
	instr6157(989),
	IF_SETPOSITION(937),
	IF_SETSIZE(982),
	IF_SETHIDE(590),
	IF_SETASPECT(434),
	IF_SETNOCLICKTHROUGH(668),
	IF_SETSCROLLPOS(825),
	IF_SETCOLOR(816),
	IF_SETFILL(918),
	IF_SETTRANS(763),
	IF_SETLINEWID(530),
	IF_SETGRAPHIC(529),
	IF_SET2DANGLE(205),
	IF_SETTILING(460),
	IF_SETMODEL(488),
	IF_SETMODELANGLE(605),
	IF_SETMODELANIM(690),
	instr6174(824),
	instr6369(82),
	IF_SETTEXT(724),
	IF_SETTEXTFONT(85),
	IF_SETTEXTALIGN(741),
	instr6170(689),
	instr6180(651),
	IF_SETBORDERTHICKNESS(344),
	instr6182(131),
	instr6183(503),
	instr6184(774),
	instr6185(628),
	instr6216(368),
	instr6187(527),
	instr6188(760),
	instr6377(862),
	instr6190(432),
	instr6191(369),
	instr6192(198),
	instr6193(479),
	instr6194(206),
	instr6195(194),
	instr6196(950),
	instr6197(316),
	IF_SETCLICKMASK(224),
	IF_SETITEM(388),
	IF_SETNPCHEAD(857),
	IF_SETPLAYERHEAD_SELF(783),
	instr6715(845),
	instr6864(547),
	instr6879(72),
	instr6205(203),
	instr6919(598),
	instr6207(518),
	instr6208(352),
	instr6209(744),
	instr6210(499),
	instr6129(552),
	instr6793(898),
	instr6213(255),
	instr6214(899),
	instr6215(495),
	instr5969(98),
	instr6217(413),
	ITEM_USEONNAME(289),
	instr6219(583),
	instr6220(512),
	instr6221(896),
	instr6222(122),
	instr6223(790),
	instr6888(805),
	instr6136(222),
	instr6226(127),
	instr6477(739),
	instr6228(903),
	instr6229(786),
	HOOK_MOUSE_PRESS(881),
	instr6231(143),
	HOOK_MOUSE_RELEASE(382),
	HOOK_MOUSE_ENTER(968),
	HOOK_MOUSE_EXIT(600),
	instr6235(478),
	instr6527(770),
	instr6237(244),
	instr6342(300),
	instr6239(172),
	instr6240(926),
	instr5973(287),
	IF_SETONMOUSEOVER(753),
	IF_SETONMOUSELEAVE(809),
	instr6393(486),
	instr6376(758),
	instr6246(550),
	instr6247(683),
	instr6248(730),
	instr6708(76),
	instr6250(625),
	instr6251(416),
	instr6252(179),
	instr6253(532),
	instr6254(947),
	instr6255(630),
	instr6256(321),
	instr6257(189),
	instr6507(768),
	instr6259(508),
	instr6260(401),
	instr6261(111),
	instr6898(105),
	IF_CLEARSCRIPTHOOKS(64),
	IF_GETX(710),
	IF_GETY(170),
	IF_GETWIDTH(263),
	IF_GETHEIGHT(953),
	IF_GETHIDE(578),
	IF_GETLAYER(135),
	IF_GETPARENTLAYER(692),
	IF_GETCOLOR(798),
	IF_GETSCROLLX(932),
	IF_GETSCROLLY(428),
	IF_GETTEXT(748),
	instr6275(496),
	IF_GETSCROLLHEIGHT(978),
	instr6277(38),
	instr6292(461),
	instr6279(96),
	instr6280(436),
	instr6281(912),
	instr6544(721),
	instr6283(463),
	instr6284(169),
	instr6072(624),
	instr6159(864),
	instr6287(870),
	instr6288(997),
	instr6289(543),
	instr6290(808),
	instr6875(373),
	instr6805(888),
	IF_GETNEXTSUBID(1000),
	instr6294(448),
	instr6295(331),
	instr6296(704),
	instr6297(164),
	instr6298(996),
	instr6299(197),
	instr6300(163),
	instr6391(52),
	instr6302(941),
	instr6303(588),
	instr6304(948),
	instr6646(999),
	instr6186(882),
	instr6307(167),
	instr6002(736),
	instr6309(250),
	MES(471),
	RESET_MYPLAYER_ANIMS(245),
	IF_CLOSE(574),
	RESUME_COUNTDIALOG(117),
	RESUME_NAMEDIALOG(216),
	RESUME_STRINGDIALOG(4),
	OPPLAYER(162),
	IF_DRAGPICKUP(18),
	CC_DRAGPICKUP(259),
	RESUME_ITEMDIALOG(304),
	IF_OPENSUBCLIENT(360),
	IF_CLOSESUBCLIENT(291),
	OPPLAYERT(637),
	MES_TYPED(913),
	SETUP_MESSAGEBOX(211),
	RESUME_HSLDIALOG(238),
	RESUME_CLANFORUMQFCDIALOG(101),
	SOUND_SYNTH(2),
	SOUND_SONG(855),
	SOUND_JINGLE(920),
	SOUND_SYNTH_VOLUME(784),
	SOUND_SONG_VOLUME(780),
	SOUND_JINGLE_VOLUME(145),
	SOUND_VORBIS_VOLUME(516),
	SOUND_SPEECH_VOLUME(959),
	SOUND_SYNTH_RATE(535),
	SOUND_VORBIS_RATE(395),
	CLIENTCLOCK(80),
	INV_GETITEM(180),
	INV_GETNUM(138),
	INV_TOTAL(485),
	INV_SIZE(157),
	INV_TOTALCAT(192),
	STAT(301),
	STAT_BASE(340),
	STAT_VISIBLE_XP(772),
	GET_PLAYER_POS(389),
	GET_PLAYER_X(93),
	GET_PLAYER_Y(298),
	GET_PLAYER_PLANE(536),
	WORLD_MEMBERS(336),
	INVOTHER_GETITEM(612),
	INVOTHER_GETNUM(720),
	INVOTHER_TOTAL(771),
	STAFFMODLEVEL(727),
	GET_SYSTEM_UPDATE_TIMER(715),
	WORLD_ID(601),
	RUNENERGY_VISIBLE(944),
	RUNWEIGHT_VISIBLE(866),
	PLAYERMOD(81),
	PLAYERMODLEVEL(718),
	PLAYERMEMBER(262),
	COMLEVEL_ACTIVE(139),
	GENDER(657),
	WORLD_QUICKCHAT(427),
	CONTAINER_FREE_SPACE(20),
	CONTAINER_TOTAL_PARAM(696),
	CONTAINER_TOTAL_PARAM_STACK(524),
	WORLD_LANGUAGE(202),
	MOVE_COORD(48),
	AFFILIATE(25),
	PROFILE_CPU(930),
	PLAYERDEMO(619),
	APPLET_HASFOCUS(781),
	FROMBILLING(655),
	GET_MOUSE_X(243),
	GET_MOUSE_Y(308),
	GET_ACTIVE_MINIMENU_ENTRY(115),
	GET_SECOND_MINIMENU_ENTRY(366),
	GET_MINIMENU_LENGTH(50),
	GET_CURRENTCURSOR(980),
	GET_SELFYANGLE(426),
	MAP_ISOWNER(214),
	GET_MOUSEBUTTONS(453),
	SELF_PLAYER_UID(554),
	GET_MINIMENU_TARGET(814),
	ENUM_STRING(820),
	ENUM(540),
	ENUM_HASOUTPUT(60),
	ENUM_HASOUTPUT_STRING(840),
	ENUM_GETOUTPUTCOUNT(533),
	ENUM_GETREVERSECOUNT(149),
	ENUM_GETREVERSECOUNT_STRING(438),
	ENUM_GETREVERSEINDEX(904),
	ENUM_GETREVERSEINDEX_STRING(86),
	EMAIL_VALIDATION_SUBMIT_CODE(364),
	EMAIL_VALIDATION_CHANGE_ADDRESS(121),
	EMAIL_VALIDATION_ADD_NEW_ADDRESS(511),
	FRIEND_COUNT(183),
	FRIEND_GETNAME(252),
	FRIEND_GETWORLD(51),
	FRIEND_GETRANK(732),
	FRIEND_GETWORLDFLAGS(562),
	FRIEND_SETRANK(647),
	FRIEND_ADD(609),
	FRIEND_DEL(914),
	IGNORE_ADD(177),
	IGNORE_DEL(871),
	FRIEND_TEST(879),
	FRIEND_GETWORLDNAME(767),
	FC_GETCHATDISPLAYNAME(765),
	FC_GETCHATCOUNT(1002),
	FC_GETCHATUSERNAME(699),
	FC_GETCHATUSERWORLD(587),
	FC_GETCHATUSERRANK(394),
	FC_GETCHATMINKICK(346),
	FC_KICKUSER(421),
	FC_GETCHATRANK(854),
	FC_JOINCHAT(293),
	FC_LEAVECHAT(682),
	IGNORE_COUNT(735),
	IGNORE_GETNAME(284),
	IGNORE_TEST(966),
	FC_ISSELF(802),
	FC_GETCHATOWNERNAME(338),
	FC_GETCHATUSERWORLDNAME(943),
	FRIEND_PLATFORM(880),
	FRIEND_GETSLOTFROMNAME(520),
	PLAYERCOUNTRY(667),
	IGNORE_ADD_TEMP(954),
	IGNORE_IS_TEMP(629),
	FC_GETCHATUSERNAME_UNFILTERED(580),
	IGNORE_GETNAME_UNFILTERED(37),
	FRIEND_IS_REFERRER(656),
	instr6434(833),
	instr6435(452),
	instr6436(815),
	instr6491(843),
	instr6841(91),
	instr6264(894),
	instr6440(549),
	instr6441(200),
	instr6442(706),
	instr6769(541),
	instr6444(166),
	instr6445(411),
	instr6446(746),
	instr6447(861),
	instr6448(962),
	instr6830(7),
	instr6160(557),
	instr6854(956),
	instr6114(104),
	instr6453(838),
	instr6454(884),
	instr6455(347),
	instr6931(370),
	instr6531(907),
	instr6787(489),
	instr6459(399),
	instr6460(444),
	instr6461(673),
	instr6245(23),
	instr5984(350),
	instr6464(897),
	instr6465(5),
	CLAN_VARS_ENABLED(559),
	instr6316(414),
	instr6468(602),
	instr6469(575),
	instr6340(27),
	instr6471(480),
	instr6472(687),
	instr6372(650),
	instr6474(733),
	instr6109(984),
	instr6266(361),
	ADD(279),
	SUBTRACT(130),
	MULTIPLY(367),
	DIVIDE(56),
	RANDOM(675),
	RANDOM_INCLUSIVE(851),
	INTERPOLATE(190),
	ADD_PERCENT(890),
	SET_BIT(119),
	CLEAR_BIT(873),
	BIT_FLAGGED(278),
	MODULO(178),
	POW(645),
	POW_INVERSE(776),
	BIT_AND(199),
	BIT_OR(474),
	MIN(538),
	MAX(750),
	SCALE(277),
	RANDOM_SOUND_PITCH(729),
	HSVTORGB(586),
	BIT_NOT(534),
	APPEND_NUM(310),
	APPEND(286),
	APPEND_SIGNNUM(573),
	GET_COL_TAG(874),
	LOWER_STRING(556),
	FROM_DATE(618),
	TEXT_GENDER(709),
	TO_STRING(688),
	STRING_EQUAL(810),
	instr6152(469),
	instr6801(43),
	instr6510(15),
	instr6511(49),
	instr6512(701),
	instr6513(638),
	instr6514(150),
	instr6515(801),
	instr6516(934),
	STRING_LENGTH(247),
	instr6518(108),
	instr6519(41),
	instr6520(593),
	instr6521(844),
	instr6522(977),
	instr6523(153),
	instr6795(280),
	instr6820(459),
	instr6881(339),
	instr6319(454),
	ITEM_NAME(385),
	ITEM_OP(128),
	ITEM_IOP(949),
	ITEM_COST(89),
	ITEM_STACKABLE(905),
	ITEM_CERT(94),
	ITEM_UNCERT(313),
	ITEM_WEARPOS(22),
	ITEM_WEARPOS2(756),
	ITEM_WEARPOS3(384),
	ITEM_MEMBERS(822),
	ITEM_PARAM(120),
	instr6540(45),
	instr6145(800),
	instr6542(623),
	instr6104(146),
	ITEM_MULTISTACKSIZE(249),
	ITEM_FIND(754),
	ITEM_FINDNEXT(264),
	ITEM_MINIMENU_COLOUR_OVERRIDDEN(481),
	ITEM_MINIMENU_COLOUR(213),
	NPC_PARAM(425),
	OBJECT_PARAM(935),
	STRUCT_PARAM(594),
	ANIMATION_PARAM(404),
	BAS_GETANIM_READY(456),
	instr6554(643),
	instr6555(990),
	instr6458(777),
	CHAT_GETFILTER_PUBLIC(860),
	CHAT_SETFILTER(424),
	SEND_REPORT_ABUSE_PACKET(257),
	instr6560(231),
	instr6739(681),
	instr6562(792),
	instr6563(118),
	instr6010(134),
	instr6211(743),
	instr6189(789),
	instr6624(622),
	instr6565(335),
	CHAT_PLAYERNAME(292),
	CHAT_GETFILTER_TRADE(839),
	instr6225(961),
	instr6572(482),
	instr6573(462),
	instr6574(309),
	instr6682(327),
	instr6249(272),
	instr6577(867),
	instr6578(927),
	instr6202(946),
	instr6030(988),
	instr6581(10),
	instr6425(604),
	instr6583(328),
	instr6584(958),
	instr6069(661),
	instr6586(187),
	instr6587(329),
	instr6588(909),
	instr6368(922),
	instr6590(713),
	instr6591(994),
	instr6592(208),
	instr6449(188),
	instr6594(795),
	instr6595(324),
	instr6596(109),
	instr6597(217),
	instr6598(349),
	instr6599(711),
	instr6613(830),
	instr6601(227),
	instr6602(285),
	instr6603(663),
	instr6233(290),
	instr6716(759),
	instr6606(334),
	instr6607(386),
	instr6608(731),
	instr6308(84),
	instr6610(397),
	instr6611(726),
	instr6612(16),
	instr6849(560),
	instr6614(405),
	instr6615(306),
	instr6616(788),
	instr6617(674),
	instr6090(441),
	instr6074(975),
	instr6620(923),
	instr6621(676),
	instr6439(410),
	instr6685(823),
	instr6638(228),
	instr6212(603),
	instr5966(260),
	instr6206(970),
	instr6628(641),
	instr6238(475),
	instr6630(969),
	instr6631(662),
	instr6632(288),
	instr6633(74),
	instr6634(901),
	instr6582(35),
	instr6671(775),
	instr6637(514),
	instr6818(210),
	instr6639(785),
	instr6488(175),
	instr6704(141),
	instr6404(596),
	instr6643(691),
	instr6644(297),
	instr6645(818),
	instr6406(617),
	instr6647(649),
	instr6648(577),
	instr6649(234),
	instr6650(567),
	instr6651(46),
	instr6652(595),
	instr6653(716),
	instr6654(951),
	instr6655(355),
	instr6656(318),
	instr6657(6),
	instr6658(431),
	instr6666(110),
	instr6660(65),
	instr6661(61),
	instr6866(565),
	instr6416(477),
	instr6410(672),
	instr5956(853),
	instr6780(964),
	instr6667(343),
	instr6668(359),
	CHECK_JAVA_VERSION(826),
	instr6044(1004),
	instr6923(973),
	instr6672(400),
	instr6673(435),
	instr6451(521),
	instr6675(570),
	instr6162(99),
	instr6677(107),
	instr6678(940),
	instr6679(144),
	instr6456(493),
	instr6681(161),
	instr6663(893),
	instr6108(868),
	instr6684(483),
	instr6823(8),
	instr6686(155),
	instr6331(931),
	instr6627(722),
	instr6236(396),
	instr6013(365),
	instr6122(151),
	instr6692(831),
	instr6204(273),
	instr6694(171),
	SEND_VERIFY_EMAIL_PACKET(654),
	SEND_SIGNUP_FORM_PACKET(686),
	instr6697(362),
	instr6698(684),
	instr6699(1003),
	instr6700(9),
	instr6323(92),
	instr6702(742),
	instr6703(498),
	instr6774(551),
	instr6641(182),
	instr6305(422),
	instr6707(19),
	instr6623(242),
	instr6709(945),
	instr6710(447),
	instr6711(391),
	instr6712(569),
	instr6642(885),
	instr6714(666),
	instr6576(875),
	instr6101(44),
	instr6717(509),
	instr6718(869),
	instr6719(506),
	instr6720(917),
	instr6721(357),
	instr6722(849),
	instr6609(484),
	instr6724(372),
	instr6725(537),
	instr6726(71),
	instr6727(226),
	instr6860(270),
	instr6729(281),
	instr6730(232),
	instr6731(685),
	instr6674(269),
	DETAIL_STEREO(383),
	DETAIL_SOUNDVOL(358),
	DETAIL_MUSICVOL(276),
	DETAIL_BGSOUNDVOL(417),
	DETAIL_REMOVEROOFS_OPTION_OVERRIDE(992),
	DETAIL_PARTICLES(703),
	DETAIL_ANTIALIASING_DEFAULT(380),
	DETAIL_BUILDAREA(376),
	DETAIL_BLOOM(564),
	instr6742(181),
	instr6743(553),
	instr6744(464),
	instr6745(348),
	instr6922(261),
	instr6747(90),
	instr6748(218),
	instr6570(568),
	instr6750(632),
	instr6629(455),
	instr6752(817),
	instr6948(332),
	instr6754(848),
	instr6285(983),
	instr6756(268),
	instr6688(254),
	instr6489(585),
	instr6759(646),
	instr6785(390),
	instr6761(891),
	GET_WATER_PREFERENCE(487),
	instr6060(193),
	instr6938(782),
	instr6755(26),
	instr6766(828),
	instr6767(942),
	instr6768(919),
	instr5975(712),
	instr6770(458),
	instr6605(582),
	instr6640(752),
	instr6773(387),
	instr6925(794),
	instr5954(419),
	instr6600(725),
	instr6777(734),
	instr6778(804),
	instr6286(418),
	instr6400(30),
	instr6781(714),
	instr6301(971),
	instr6783(353),
	instr6784(42),
	instr6941(132),
	instr6786(240),
	instr6775(737),
	instr6097(423),
	instr6789(581),
	instr6790(607),
	instr6764(745),
	instr6792(47),
	instr6141(936),
	instr6486(403),
	instr5988(283),
	instr6365(55),
	instr6797(519),
	instr6798(636),
	instr6799(793),
	instr6800(877),
	instr6821(17),
	instr6802(256),
	instr6803(233),
	instr6227(212),
	instr6829(915),
	instr6806(239),
	instr6807(960),
	instr6808(296),
	instr6809(658),
	WORLDLIST_PINGWORLDS(678),
	instr6589(303),
	IF_DEBUG_GETOPENIFCOUNT(271),
	IF_DEBUG_GETOPENIFID(342),
	IF_DEBUG_GETNAME(738),
	IF_DEBUG_GETCOMCOUNT(136),
	IF_DEBUG_GETCOMNAME(40),
	IF_DEBUG_GETSERVERTRIGGERS(307),
	instr6386(429),
	instr6659(305),
	instr6794(653),
	instr6278(561),
	instr6890(294),
	instr6350(819),
	instr6824(251),
	instr6825(31),
	instr6826(778),
	instr6827(266),
	instr6828(906),
	instr6920(445),
	instr5955(437),
	MEC_TEXT(985),
	MEC_SPRITE(631),
	MEC_TEXTSIZE(757),
	MEC_CATEGORY(976),
	MEC_PARAM(230),
	USERDETAIL_QUICKCHAT(201),
	USERDETAIL_LOBBY_MEMBERSHIP(148),
	USERDETAIL_LOBBY_RECOVERYDAY(379),
	USERDETAIL_LOBBY_UNREADMESSAGES(241),
	USERDETAIL_LOBBY_LASTLOGINDAY(236),
	USERDETAIL_LOBBY_LASTLOGINADDRESS(78),
	USERDETAIL_LOBBY_EMAILSTATUS(54),
	USERDETAIL_LOBBY_CCEXPIRY(679),
	USERDETAIL_LOBBY_GRACEEXPIRY(522),
	USERDETAIL_LOBBY_DOBREQUESTED(769),
	USERDETAIL_DOB(407),
	USERDETAIL_LOBBY_MEMBERSSTATS(253),
	USERDETAIL_LOBBY_PLAYAGE(393),
	USERDETAIL_LOBBY_JCOINS_BALANCE(887),
	USERDETAIL_LOBBY_LOYALTY_ENABLED(265),
	USERDETAIL_LOBBY_LOYALTY_BALANCE(375),
	instr6852(1005),
	AUTOSETUP_SETHIGH(398),
	AUTOSETUP_SETMEDIUM(837),
	AUTOSETUP_SETLOW(225),
	AUTOSETUP_SETMIN(955),
	instr6857(312),
	instr6858(773),
	instr6859(952),
	instr6046(174),
	instr6861(88),
	instr6862(762),
	instr6863(548),
	instr6838(766),
	instr6680(507),
	instr6476(576),
	instr6867(106),
	instr6868(420),
	instr6869(337),
	instr6058(63),
	instr6871(761),
	instr6779(129),
	instr6873(640),
	instr6874(635),
	instr6041(621),
	instr6876(803),
	instr6877(221),
	instr6878(610),
	instr6776(504),
	instr6880(938),
	instr6822(446),
	instr6882(563),
	instr6883(258),
	instr6884(850),
	instr6885(821),
	instr6886(114),
	instr6887(1001),
	instr6760(58),
	instr6156(442),
	instr6123(900),
	instr6891(876),
	instr6892(571),
	instr6872(501),
	instr6894(865),
	instr6622(33),
	instr5997(921),
	instr6543(409),
	instr6433(779),
	instr6173(616),
	instr6900(53),
	instr6388(430),
	instr6902(282),
	instr6203(378),
	instr6904(812),
	instr6905(740),
	instr6906(29),
	instr6907(660),
	instr6908(165),
	instr6664(330),
	instr5946(811),
	instr6911(669),
	instr6028(248),
	instr6913(634),
	instr6107(322),
	IS_TARGETED_ENTITY(852),
	instr6916(986),
	NPC_TYPE(974),
	GET_OBJECT_SCREEN_POSITION(439),
	GET_ITEM_SCREEN_POSITION(450),
	GET_OBJECT_OVERLAY_HEIGHT(433),
	GET_ITEM_OVERLAY_HEIGHT(295),
	GET_OBJECT_BOUNDING_BOX(69),
	GET_ITEM_BOUNDING_BOX(517),
	GET_ENTITY_BOUNDING_BOX(95),
	BUG_REPORT(558),
	ARRAY_SORT(87),
	QUEST_GETNAME(791),
	QUEST_GETSORTNAME(889),
	QUEST_TYPE(842),
	QUEST_GETDIFFICULTY(468),
	QUEST_GETMEMBERS(863),
	QUEST_POINTS(608),
	QUEST_QUESTREQ_COUNT(14),
	QUEST_QUESTREQ(642),
	QUEST_QUESTREQ_MET(856),
	QUEST_POINTSREQ(846),
	QUEST_POINTSREQ_MET(967),
	QUEST_STATREQ_COUNT(102),
	QUEST_STATREQ_STAT(614),
	QUEST_STATREQ_LEVEL(39),
	QUEST_STATREQ_MET(592),
	QUEST_VARPREQ_COUNT(698),
	QUEST_VARPREQ_DESC(302),
	QUEST_VARPREQ_MET(299),
	QUEST_VARBITREQ_COUNT(11),
	QUEST_VARBITREQ_DESC(835),
	QUEST_VARBITREQ_MET(858),
	QUEST_ALLREQMET(152),
	QUEST_STARTED(12),
	QUEST_FINISHED(963),
	QUEST_PARAM(62);

	private static HashMap<Integer, CS2Instruction> OPCODES = new HashMap<>();
	
	static {
		for (CS2Instruction op : CS2Instruction.values()) {
			OPCODES.put(op.opcode, op);
		}
	}
	
	public static CS2Instruction getByOpcode(int id) {
		return OPCODES.get(id);
	}
	
	public int opcode;
	public boolean hasIntConstant;

	private CS2Instruction(int opcode) {
		this(opcode, false);
	}

	private CS2Instruction(int opcode, boolean hasIntConstant) {
		this.opcode = opcode;
		this.hasIntConstant = hasIntConstant;
	}

	public int getOpcode() {
		return opcode;
	}

	public boolean hasIntConstant() {
		return hasIntConstant;
	}
}
