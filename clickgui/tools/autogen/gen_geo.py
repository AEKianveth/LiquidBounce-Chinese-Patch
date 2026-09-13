# -*- coding: utf-8 -*-
# 代理“国家/地区”下拉的地理名词（洲/大区/国家/首都）标准中文对照，走 __zh 显示。
import json
CONT={'Africa':'非洲','Americas':'美洲','Antarctica':'南极洲','Asia':'亚洲','Atlantic Ocean':'大西洋','Europe':'欧洲','Indian Ocean':'印度洋','Oceania':'大洋洲'}
REG={'Antarctica':'南极洲','Central Africa':'中非','Central America':'中美洲','Central Asia':'中亚','Central Europe':'中欧','East Asia':'东亚','Eastern Africa':'东非','Eastern Europe':'东欧','Indian Ocean':'印度洋','North America':'北美','Northern Africa':'北非','Northern Asia':'北亚','Northern Europe':'北欧','Pacific':'太平洋','South America':'南美','South Asia':'南亚','South Atlantic Ocean':'南大西洋','South East Asia':'东南亚','South East Europe':'东南欧','South West Asia':'西南亚','South West Europe':'西南欧','Southern Africa':'南部非洲','Southern Europe':'南欧','Southern Indian Ocean':'南印度洋','West Indies':'西印度群岛','Western Africa':'西非','Western Europe':'西欧'}
COUNTRY={
'Afghanistan':'阿富汗','Albania':'阿尔巴尼亚','Algeria':'阿尔及利亚','American Samoa':'美属萨摩亚','Andorra':'安道尔','Angola':'安哥拉','Anguilla':'安圭拉','Antarctica':'南极洲','Antigua and Barbuda':'安提瓜和巴布达','Argentina':'阿根廷','Armenia':'亚美尼亚','Aruba':'阿鲁巴','Australia':'澳大利亚','Austria':'奥地利','Azerbaijan':'阿塞拜疆',
'Bahrain':'巴林','Bangladesh':'孟加拉国','Barbados':'巴巴多斯','Belarus':'白俄罗斯','Belgium':'比利时','Belize':'伯利兹','Benin':'贝宁','Bermuda':'百慕大','Bhutan':'不丹','Bolivia':'玻利维亚','Bonaire':'博奈尔','Bosnia and Herzegovina':'波黑','Botswana':'博茨瓦纳','Bouvet Island':'布韦岛','Brazil':'巴西','British Indian Ocean Territory':'英属印度洋领地','British Virgin Islands':'英属维尔京群岛','Brunei':'文莱','Bulgaria':'保加利亚','Burkina Faso':'布基纳法索','Burundi':'布隆迪',
'Cambodia':'柬埔寨','Cameroon':'喀麦隆','Canada':'加拿大','Cape Verde':'佛得角','Cayman Islands':'开曼群岛','Central African Republic':'中非共和国','Chad':'乍得','Chile':'智利','China':'中国','Christmas Island':'圣诞岛','Cocos (Keeling) Islands':'科科斯（基林）群岛','Colombia':'哥伦比亚','Comoros':'科摩罗','Cook Islands':'库克群岛','Costa Rica':'哥斯达黎加',"Cote d'Ivoire":'科特迪瓦','Croatia':'克罗地亚','Cuba':'古巴','Curaçao':'库拉索','Cyprus':'塞浦路斯','Czechia':'捷克',
'Democratic Republic of the Congo':'刚果（金）','Denmark':'丹麦','Djibouti':'吉布提','Dominica':'多米尼克','Dominican Republic':'多米尼加',
'Ecuador':'厄瓜多尔','Egypt':'埃及','El Salvador':'萨尔瓦多','Equatorial Guinea':'赤道几内亚','Eritrea':'厄立特里亚','Estonia':'爱沙尼亚','Eswatini':'斯威士兰','Ethiopia':'埃塞俄比亚',
'Falkland Islands (Islas Malvinas)':'福克兰群岛','Faroe Islands':'法罗群岛','Federated States of Micronesia':'密克罗尼西亚联邦','Fiji':'斐济','Finland':'芬兰','France':'法国','French Guiana':'法属圭亚那','French Polynesia':'法属波利尼西亚','French Southern and Antarctic Lands':'法属南部和南极领地',
'Gabon':'加蓬','Georgia':'格鲁吉亚','Germany':'德国','Ghana':'加纳','Gibraltar':'直布罗陀','Greece':'希腊','Greenland':'格陵兰','Grenada':'格林纳达','Guadeloupe':'瓜德罗普','Guam':'关岛','Guatemala':'危地马拉','Guernsey':'根西岛','Guinea':'几内亚','Guinea-Bissau':'几内亚比绍','Guyana':'圭亚那',
'Haiti':'海地','Heard Island and McDonald Islands':'赫德岛和麦克唐纳群岛','Holy See (Vatican City)':'梵蒂冈','Honduras':'洪都拉斯','Hong Kong':'中国香港','Hungary':'匈牙利',
'Iceland':'冰岛','India':'印度','Indonesia':'印度尼西亚','Iran':'伊朗','Iraq':'伊拉克','Ireland':'爱尔兰','Isle of Man':'马恩岛','Israel':'以色列','Italy':'意大利',
'Jamaica':'牙买加','Japan':'日本','Jersey':'泽西岛','Jordan':'约旦',
'Kazakhstan':'哈萨克斯坦','Kenya':'肯尼亚','Kiribati':'基里巴斯','Kuwait':'科威特','Kyrgyzstan':'吉尔吉斯斯坦',
'Laos':'老挝','Latvia':'拉脱维亚','Lebanon':'黎巴嫩','Lesotho':'莱索托','Liberia':'利比里亚','Libya':'利比亚','Liechtenstein':'列支敦士登','Lithuania':'立陶宛','Luxembourg':'卢森堡',
'Macau':'中国澳门','Madagascar':'马达加斯加','Malawi':'马拉维','Malaysia':'马来西亚','Maldives':'马尔代夫','Mali':'马里','Malta':'马耳他','Marshall Islands':'马绍尔群岛','Martinique':'马提尼克','Mauritania':'毛里塔尼亚','Mauritius':'毛里求斯','Mayotte':'马约特','Mexico':'墨西哥','Moldova':'摩尔多瓦','Monaco':'摩纳哥','Mongolia':'蒙古','Montenegro':'黑山','Montserrat':'蒙特塞拉特','Morocco':'摩洛哥','Mozambique':'莫桑比克','Myanmar (Burma)':'缅甸',
'Namibia':'纳米比亚','Nauru':'瑙鲁','Nepal':'尼泊尔','Netherlands':'荷兰','Netherlands Antilles':'荷属安的列斯','New Caledonia':'新喀里多尼亚','New Zealand':'新西兰','Nicaragua':'尼加拉瓜','Niger':'尼日尔','Nigeria':'尼日利亚','Niue':'纽埃','Norfolk Island':'诺福克岛','North Korea':'朝鲜','North Macedonia':'北马其顿','Northern Mariana Islands':'北马里亚纳群岛','Norway':'挪威',
'Oman':'阿曼','Pakistan':'巴基斯坦','Palau':'帕劳','Palestinian Territory':'巴勒斯坦地区','Panama':'巴拿马','Papua New Guinea':'巴布亚新几内亚','Paraguay':'巴拉圭','Peru':'秘鲁','Philippines':'菲律宾','Pitcairn Islands':'皮特凯恩群岛','Poland':'波兰','Portugal':'葡萄牙','Puerto Rico':'波多黎各',
'Qatar':'卡塔尔','Republic of Kosovo':'科索沃','Republic of the Congo':'刚果（布）','Reunion':'留尼汪','Romania':'罗马尼亚','Russia':'俄罗斯','Rwanda':'卢旺达',
'Saint Barthélemy':'圣巴泰勒米','Saint Helena':'圣赫勒拿','Saint Kitts and Nevis':'圣基茨和尼维斯','Saint Lucia':'圣卢西亚','Saint Martin':'圣马丁','Saint Pierre and Miquelon':'圣皮埃尔和密克隆','Saint Vincent and the Grenadines':'圣文森特和格林纳丁斯','San Marino':'圣马力诺','Sao Tome and Principe':'圣多美和普林西比','Saudi Arabia':'沙特阿拉伯','Senegal':'塞内加尔','Serbia':'塞尔维亚','Seychelles':'塞舌尔','Sierra Leone':'塞拉利昂','Singapore':'新加坡','Sint Maarten':'圣马丁（荷属）','Slovakia':'斯洛伐克','Slovenia':'斯洛文尼亚','Solomon Islands':'所罗门群岛','Somalia':'索马里','South Africa':'南非','South Georgia and the South Sandwich Islands':'南乔治亚和南桑威奇群岛','South Korea':'韩国','South Sudan':'南苏丹','Spain':'西班牙','Sri Lanka':'斯里兰卡','Sudan':'苏丹','Suriname':'苏里南','Svalbard':'斯瓦尔巴','Sweden':'瑞典','Switzerland':'瑞士','Syria':'叙利亚',
'Taiwan':'中国台湾','Tajikistan':'塔吉克斯坦','Tanzania':'坦桑尼亚','Thailand':'泰国','The Bahamas':'巴哈马','The Gambia':'冈比亚','Timor-Leste':'东帝汶','Togo':'多哥','Tokelau':'托克劳','Tonga':'汤加','Trinidad and Tobago':'特立尼达和多巴哥','Tunisia':'突尼斯','Turkey':'土耳其','Turkmenistan':'土库曼斯坦','Turks and Caicos Islands':'特克斯和凯科斯群岛','Tuvalu':'图瓦卢',
'Uganda':'乌干达','Ukraine':'乌克兰','United Arab Emirates':'阿联酋','United Kingdom':'英国','United States':'美国','United States Minor Outlying Islands':'美国本土外小岛屿','Uruguay':'乌拉圭','Uzbekistan':'乌兹别克斯坦',
'Vanuatu':'瓦努阿图','Venezuela':'委内瑞拉','Vietnam':'越南','Virgin Islands':'美属维尔京群岛',
'Wallis and Futuna':'瓦利斯和富图纳','Western Sahara':'西撒哈拉','Western Samoa':'西萨摩亚',
'Yemen':'也门','Zambia':'赞比亚','Zimbabwe':'津巴布韦','Åland Islands':'奥兰群岛'}
CAP={
'Abu Dhabi':'阿布扎比','Abuja':'阿布贾','Accra':'阿克拉','Adamstown':'亚当斯敦','Addis Ababa':'亚的斯亚贝巴','Algiers':'阿尔及尔','Alofi':'阿洛菲','Amman':'安曼','Amsterdam':'阿姆斯特丹','Andorra la Vella':'安道尔城','Ankara':'安卡拉','Antananarivo':'塔那那利佛','Apia':'阿皮亚','Ashgabat':'阿什哈巴德','Asmara':'阿斯马拉','Astana (Akmola)':'阿斯塔纳','Asuncion':'亚松森','Athens':'雅典','Avarua':'阿瓦鲁阿',
'Baghdad':'巴格达','Baku (Baki)':'巴库','Bamako':'巴马科','Bandar Seri Begawan':'斯里巴加湾市','Bangkok':'曼谷','Bangui':'班吉','Banjul':'班珠尔','Basse-Terre':'巴斯特尔','Basseterre':'巴斯特尔','Beijing':'北京','Beirut':'贝鲁特','Belgrade':'贝尔格莱德','Belmopan':'贝尔莫潘','Berlin':'柏林','Bern':'伯尔尼','Bishkek':'比什凯克','Bissau':'比绍','Bogota':'波哥大','Brasilia':'巴西利亚','Bratislava':'布拉迪斯拉发','Brazzaville':'布拉柴维尔','Bridgetown':'布里奇敦','Brussels':'布鲁塞尔','Bucharest':'布加勒斯特','Budapest':'布达佩斯','Buenos Aires':'布宜诺斯艾利斯','Bujumbura':'布琼布拉',
'Cairo':'开罗','Canberra':'堪培拉','Caracas':'加拉加斯','Castries':'卡斯特里','Cayenne':'卡宴','Charlotte Amalie':'夏洛特阿马利亚','Chisinau':'基希讷乌','Colombo':'科伦坡','Conakry':'科纳克里','Copenhagen':'哥本哈根',
'Dakar':'达喀尔','Damascus':'大马士革','Dar es Salaam':'达累斯萨拉姆','Dhaka':'达卡','Dili':'帝力','Djibouti':'吉布提市','Doha':'多哈','Douglas':'道格拉斯','Dublin':'都柏林','Dushanbe':'杜尚别',
'East Jerusalem':'东耶路撒冷','Fort-de-France':'法兰西堡','Freetown':'弗里敦','Funafuti':'富纳富提',
'Gaborone':'哈博罗内','George Town':'乔治敦','Georgetown':'乔治城','Gibraltar':'直布罗陀','Grand Turk':'大特克','Guatemala':'危地马拉城','Gustavia':'古斯塔维亚','Hagatna (Agana)':'阿加尼亚','Hamilton':'哈密尔顿','Hanoi':'河内','Harare':'哈拉雷','Havana':'哈瓦那','Helsinki':'赫尔辛基','Honiara':'霍尼亚拉',
'Islamabad':'伊斯兰堡','Jakarta':'雅加达','Jerusalem':'耶路撒冷','Juba':'朱巴',
'Kabul':'喀布尔','Kampala':'坎帕拉','Kathmandu':'加德满都','Khartoum':'喀土穆','Kigali':'基加利','Kingston':'金斯顿','Kingstown':'金斯敦','Kinshasa':'金沙萨','Koror':'科罗尔','Kralendijk':'克拉伦代克','Kuala Lumpur':'吉隆坡','Kuwait':'科威特城','Kyiv':'基辅',
'La Paz / Sucre':'拉巴斯/苏克雷','Libreville':'利伯维尔','Lilongwe':'利隆圭','Lima':'利马','Lisbon':'里斯本','Ljubljana':'卢布尔雅那','Lome':'洛美','London':'伦敦','Longyearbyen':'朗伊尔城','Luanda':'罗安达','Lusaka':'卢萨卡','Luxembourg':'卢森堡',
'Macau':'澳门','Madrid':'马德里','Majuro':'马朱罗','Malabo':'马拉博','Male (Maale)':'马累','Mamoutzou':'马穆楚','Managua':'马那瓜','Manama':'麦纳麦','Manila':'马尼拉','Maputo':'马普托','Mariehamn':'玛丽港','Marigot':'马里戈特','Maseru':'马塞卢','Mata-Utu (on Ile Uvea)':'马塔乌图','Mbabane':'姆巴巴内','Mexico':'墨西哥城','Minsk':'明斯克','Mogadishu':'摩加迪沙','Monaco':'摩纳哥','Monrovia':'蒙罗维亚','Montevideo':'蒙得维的亚','Moroni':'莫罗尼','Moscow':'莫斯科','Muscat':'马斯喀特',
"N'Djamena":'恩贾梅纳','Nairobi':'内罗毕','Nassau':'拿骚','New Delhi':'新德里','Niamey':'尼亚美','Nicosia':'尼科西亚','Nouakchott':'努瓦克肖特','Noumea':'努美阿',"Nuku'alofa":'努库阿洛法','Nuuk (Godthab)':'努克',
'Oranjestad':'奥拉涅斯塔德','Oslo':'奥斯陆','Ottawa':'渥太华','Ouagadougou':'瓦加杜古',
"P'yongyang":'平壤','Pago Pago':'帕果帕果','Palikir':'帕利基尔','Panama':'巴拿马城','Papeete':'帕皮提','Paramaribo':'帕拉马里博','Paris':'巴黎','Philipsburg':'菲利普斯堡','Phnom Penh':'金边','Plymouth':'普利茅斯','Podgorica':'波德戈里察','Port Louis':'路易港','Port Moresby':'莫尔兹比港','Port-Vila':'维拉港','Port-au-Prince':'太子港','Port-of-Spain':'西班牙港','Porto-Novo':'波多诺伏','Prague':'布拉格','Praia':'普拉亚','Pristina':'普里什蒂纳',
'Quito':'基多','Rabat':'拉巴特','Rangoon (Yangon)':'仰光','Reykjavik':'雷克雅未克','Riga':'里加','Riyadh':'利雅得','Road Town':'罗德城','Rome':'罗马','Roseau':'罗索',
"Saint George's":'圣乔治','Saint Helier':'圣赫利尔',"Saint John's":'圣约翰','Saint Peter Port':'圣彼得港','Saint-Denis':'圣但尼','Saint-Pierre':'圣皮埃尔','Saipan':'塞班','San Jose':'圣何塞','San Juan':'圣胡安','San Marino':'圣马力诺城','San Salvador':'圣萨尔瓦多','Sanaa':'萨那','Santiago':'圣地亚哥','Santo Domingo':'圣多明各','Sao Tome':'圣多美','Sarajevo':'萨拉热窝','Seoul':'首尔','Singapore':'新加坡','Skopje':'斯科普里','Sofia':'索非亚','Stanley':'斯坦利','Stockholm':'斯德哥尔摩','Suva':'苏瓦',
"T'bilisi":'第比利斯','Taipei':'台北','Tallinn':'塔林','Tarawa':'塔拉瓦','Tashkent (Toshkent)':'塔什干','Tegucigalpa':'特古西加尔巴','Tehran':'德黑兰','The Settlement':'定居点','The Valley':'瓦利','Thimphu':'廷布','Tirana':'地拉那','Tokyo':'东京','Torshavn':'托尔斯港','Tripoli':'的黎波里','Tunis':'突尼斯城',
'Ulaanbaatar':'乌兰巴托','Vaduz':'瓦杜兹','Valletta':'瓦莱塔','Vatican City':'梵蒂冈城','Victoria':'维多利亚','Vienna':'维也纳','Vientiane':'万象','Vilnius':'维尔纽斯',
'Warsaw':'华沙','Washington DC':'华盛顿','Wellington':'惠灵顿','West Island':'西岛','Willemstad':'威廉斯塔德','Windhoek':'温得和克',
'Yamoussoukro':'亚穆苏克罗','Yaounde':'雅温得','Yerevan':'埃里温','Zagreb':'萨格勒布','no official capital':'无正式首都','none':'无'}
geo=json.load(open('/tmp/geo.json'))
allmap={}
for d in (CONT,REG,COUNTRY,CAP): allmap.update(d)
miss=[]
out={}
for f in ('continent','region','country','capital'):
    for v in geo[f]:
        if v in allmap: out[v]=allmap[v]
        else: miss.append((f,v))
# Pretoria 带特殊字符，按实际 key 补
print("缺失:",miss)
json.dump(dict(sorted(out.items())),open('tools/data/geo_zh.json','w',encoding='utf-8'),ensure_ascii=False,indent=1)
print("geo_zh 词条:",len(out))
